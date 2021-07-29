package māia.ml.dataset.type.standard

import māia.ml.dataset.type.Numeric

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
object NumericFloatImpl : Numeric<Float>() {

    override fun convertToInternal(value : Double) : Float {
        return value.toFloat()
    }

    override fun convertToExternal(value : Float) : Double {
        return value.toDouble()
    }

    override fun isValidInternal(value : Float) : Boolean {
        return true
    }

}
