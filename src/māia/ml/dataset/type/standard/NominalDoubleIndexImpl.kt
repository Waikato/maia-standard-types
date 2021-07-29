package māia.ml.dataset.type.standard

import māia.ml.dataset.type.Nominal

/**
 * Implementation of the nominal data-type which internally represents
 * values with their index in the list of categories (as doubles).
 */
class NominalDoubleIndexImpl(vararg categories : String) : Nominal<Double>(*categories)
{
    override fun initial() : Double {
        return 0.0
    }

    override fun convertToExternal(value : Double) : String {
        return this[value.toInt()]
    }

    override fun convertToInternal(value : String) : Double {
        return indexOf(value).toDouble()
    }

    override fun isValidInternal(value : Double) : Boolean {
        return value.toInt() in categoryIndices
    }

    override fun indexOfInternal(category : Double) : Int {
        return category.toInt()
    }

    override fun convertIndexToInternal(index : Int) : Double {
        return index.toDouble()
    }
}
