package māia.ml.dataset.type.standard

import māia.ml.dataset.type.Numeric

/**
 * Represents a column of real-valued numeric data.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
object NumericDoubleImpl : Numeric<Double>() {

    override fun convertToInternal(value : Double) : Double {
        return value
    }

    override fun convertToExternal(value : Double) : Double {
        return value
    }

    override fun isValidInternal(value : Double) : Boolean {
        return true
    }

}
