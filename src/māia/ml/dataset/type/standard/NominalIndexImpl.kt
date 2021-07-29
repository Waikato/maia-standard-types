package māia.ml.dataset.type.standard

import māia.ml.dataset.type.Nominal

/**
 * Implementation of the nominal data-type which internally represents
 * values with their index in the list of categories.
 */
class NominalIndexImpl(vararg categories : String) : Nominal<Int>(*categories)
{
    override fun initial() : Int {
        return 0
    }

    override fun convertToExternal(value : Int) : String {
        return this[value]
    }

    override fun convertToInternal(value : String) : Int {
        return indexOf(value)
    }

    override fun isValidInternal(value : Int) : Boolean {
        return value in categoryIndices
    }

    override fun indexOfInternal(category : Int) : Int {
        return category
    }

    override fun convertIndexToInternal(index : Int) : Int {
        return index
    }
}
