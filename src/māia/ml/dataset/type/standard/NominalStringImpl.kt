package māia.ml.dataset.type.standard

import māia.ml.dataset.type.Nominal
import kotlin.Exception

/**
 * Represents data which falls into one of a number of named
 * categories.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class NominalStringImpl(vararg categories : String) : Nominal<String>(*categories) {
    override fun initial() : String {
        return this[0]
    }

    override fun convertToInternal(value : String) : String {
        if (!isCategory(value)) throw Exception("$value is not a category")
        return value
    }

    override fun convertToExternal(value : String) : String {
        return value
    }

    override fun isValidInternal(value : String) : Boolean {
        return isCategory(value)
    }

}
