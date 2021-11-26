# Scrubber
> This project is still very  early in development and should not be used in a production environment

Scrubber is a lightweight Kotlin compiler plugin that generates easy-to-use type safe code for scrubbing data from your objects.

## Example Usage
Scrubber works by looking for the `@Scrub` annotation on fields or objects within your project.
```kotlin
@Scrub
data class DataClass(val address: String, val city: String, val unit: Int)

class Human(
    private var firstName: String,
    @Scrub
    var lastName: String,
    @Scrub
    var socialSecurityNumber: String
)
```
Given the example above, Scrubber will generate the following class in your generated sources folder
```kotlin
public class Scrubber {
  public val fields: Set<String> = setOf("address","city","lastName","socialSecurityNumber")

  private fun scrubDataClass(obj: DataClass): DataClass = obj.copy(address = "****",city = "****")

  private fun scrubHuman(obj: Human): Human {
    obj.lastName = "****"
    obj.socialSecurityNumber = "****"
    return obj
  }

  public fun <T> scrub(obj: T): T {
    if (obj is DataClass) {
      return scrubDataClass(obj) as T
    }
    if (obj is Human) {
      return scrubHuman(obj) as T
    }
    return obj
  }
}
```
From your source code you may now call `scrub(obj: T` with any object. If that object has any fields marked as `Scrub`, those fields will be removed.
The `Scrubber` class always exposes a `fields: Set<String>` containing a list of all fields that are marked in your project if you need.

## Project Status
This project is very new, and I would not recommend using it in any production environment just yet. It will be released on maven central once I consider it stable enough to use.

### Future ideas:
- Supporting more than just scrubbing `String`
- Allowing the `Scrubber` class to accept a class that overrides the default override value (Currently "****") 
- Supporting custom objects outside the Kotlin standard library

If you have any more ideas, I'd love to [hear them.](https://github.com/HaydenMeloche/scrubber/issues)
