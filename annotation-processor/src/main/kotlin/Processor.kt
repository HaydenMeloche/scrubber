import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    val allProperties = mutableSetOf<KSPropertyDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.example.annotation.Scrub")
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration || it is KSPropertyDeclaration && it.validate() }
            .forEach { it.accept(BuilderVisitor(), Unit) }
        return ret
    }

    override fun finish() {
        // time to generate the data
        Generator.writeScrubber(allProperties, codeGenerator)
    }


    inner class BuilderVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        // handle scrub
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            val parent = property.parentDeclaration as KSClassDeclaration
            allProperties.add(property)

            // if property isn't mutable then we need to check if it's part of a data class
            if (!property.isMutable) {
                val hasCopyFunction = parent.getAllFunctions().any { it.toString() == "copy" }
                if (!hasCopyFunction) {
                    throw IllegalStateException("Cannot process property: $property. The property must be mutable or part a data-class")
                }
            }

        }

        // handle @Scrub on top of a class
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val parent = function.parentDeclaration as KSClassDeclaration
            parent.getAllProperties().forEach { visitPropertyDeclaration(it, data) }
        }
    }

}

class ProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return Processor(env.codeGenerator, env.logger)
    }
}