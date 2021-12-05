package dev.meloche

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

object Generator {

    @OptIn(KotlinPoetKspPreview::class)
    fun writeScrubber(allProperties: Set<KSPropertyDeclaration>, codeGenerator: CodeGenerator) {
        val pack = "dev.meloche.scrubber"

        val fileName = "Scrubber"
        val fileBuilder = FileSpec.builder(pack, fileName)
        fileBuilder.addImport("dev.meloche", "NullScrubProcessor")
        val classBuilder = TypeSpec.classBuilder(fileName).primaryConstructor(
            FunSpec.constructorBuilder()
                .addStatement("this.scrubbingProcessor = NullScrubProcessor()")
                .build()
        )


        classBuilder.addProperty(
            PropertySpec.builder("scrubbingProcessor", CustomScubProcessor::class, KModifier.PRIVATE).mutable(true).build()
        )

        classBuilder.addFunction(
            FunSpec.builder("Scrubber").addParameter("customScrubber", CustomScubProcessor::class).addStatement("scrubbingProcessor = customScrubber").build()
        )

        // setup set of fields
        var scrubbedFields = ""
        allProperties.forEach {
            scrubbedFields += "\"${it}\","
        }
        scrubbedFields = scrubbedFields.dropLast(1)
        classBuilder.addProperty(
            PropertySpec.builder("fields", Set::class.parameterizedBy(String::class))
                .initializer("setOf($scrubbedFields)").build()
        )

        val generatedFunctions = mutableSetOf<String>()

        // create scrubbing functions
        allProperties.distinctBy { it.parentDeclaration }
            .map { it.parentDeclaration as KSClassDeclaration }
            .forEach {
                val hasCopyFunction =
                    it.getAllFunctions().any { function -> function.toString() == "copy" }

                // if the class has a copy function then we default to using that
                val funSpec = FunSpec.builder("scrub$it")
                    .addModifiers(KModifier.PRIVATE)
                    .addParameter("obj", it.toClassName())
                    .returns(it.toClassName())

                // when the class has a copy function we prefer to use this for immutability
                if (hasCopyFunction) {
                    var copyFields = ""
                    allProperties.filter { property -> property.parentDeclaration == it && property.type.toString() == "String" }
                        .forEach { property ->
                            copyFields += "$property = scrubbingProcessor.getValue(obj.$property, ${property.type}::class),"
                        }
                    copyFields = copyFields.dropLast(1)
                    funSpec.addStatement("return obj.copy($copyFields)")
                } else {
                    allProperties.filter { property -> property.parentDeclaration == it }
                        .forEach { property ->
                            funSpec.addStatement("obj.$property = scrubbingProcessor.getValue(obj.$property, ${property.type}::class)")
                        }
                    funSpec.addStatement("return obj")
                }
                classBuilder.addFunction(funSpec.build())
                generatedFunctions.add(it.toString())
            }

        val genericT = TypeVariableName("T")
        val mainFunction = FunSpec.builder("scrub")
            .addTypeVariable(genericT)
            .addParameter("obj", genericT)
            .returns(genericT)

        generatedFunctions.forEach {
            mainFunction.beginControlFlow("if (obj is $it)")
            mainFunction.addStatement("return scrub$it(obj) as T")
            mainFunction.endControlFlow()
        }
        mainFunction.addStatement("return obj")

        classBuilder.addFunction(mainFunction.build())

        val file = fileBuilder.addType(classBuilder.build()).build()
        val output =
            codeGenerator.createNewFile(Dependencies(true), "dev.meloche.scrubber", "Scrubber")
        output.write(file.toString().toByteArray())
        output.close()
    }

    fun test(clazz: KClass<*>): Unit {

    }
}