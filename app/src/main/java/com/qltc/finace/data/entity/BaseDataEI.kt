package com.qltc.finace.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


abstract class BaseDataEI(
    var id: String? = null,
    var idCate: String? = null,
    var uuidUser: String? = null,
    var dateEI: String? = null,
    var money: Long? = null,
    var noteEI: String? = null
) : Parcelable{
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (idCate?.hashCode() ?: 0)
        result = 31 * result + (uuidUser?.hashCode() ?: 0)
        result = 31 * result + (dateEI?.hashCode() ?: 0)
        result = 31 * result + (money?.hashCode() ?: 0)
        result = 31 * result + (noteEI?.hashCode() ?: 0)
        return result
    }


}