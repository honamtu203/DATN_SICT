package com.qltc.finace.data.entity

import com.qltc.finace.R
object Icon{
    private val iconMap : Map<String,Int> = mapOf(
        "ic_1" to R.drawable.ic_1,
        "ic_2" to R.drawable.ic_2,
        "ic_3" to R.drawable.ic_3,
        "ic_4" to R.drawable.ic_4,
        "ic_5" to R.drawable.ic_5,
        "ic_6" to R.drawable.ic_6,
        "ic_7" to R.drawable.ic_7,
        "ic_8" to R.drawable.ic_8,
        "ic_9" to R.drawable.ic_9,
        "ic_10" to R.drawable.ic_10,
        "ic_11" to R.drawable.ic_11,
        "ic_12" to R.drawable.ic_12,
        "ic_13" to R.drawable.ic_13,
        "ic_14" to R.drawable.ic_14,
        "ic_15" to R.drawable.ic_15,
        "ic_16" to R.drawable.ic_16,
        "ic_17" to R.drawable.ic_17,
        "ic_18" to R.drawable.ic_18,
        "ic_19" to R.drawable.ic_19,
        "ic_20" to R.drawable.ic_20,
        "ic_21" to R.drawable.ic_21,
        "ic_22" to R.drawable.ic_22,
        "ic_23" to R.drawable.ic_23,
        "ic_24" to R.drawable.ic_24,
        "ic_25" to R.drawable.ic_25,
        "ic_26" to R.drawable.ic_26,
        "ic_27" to R.drawable.ic_27,
        "ic_28" to R.drawable.ic_28,
        "ic_29" to R.drawable.ic_29,
        "ic_30" to R.drawable.ic_30,
        "ic_31" to R.drawable.ic_31,
        "ic_32" to R.drawable.ic_32,
        "ic_33" to R.drawable.ic_logo_facebook,
        "ic_34" to R.drawable.ic_google,
        "ic_35" to R.drawable.ic_input,
        "ic_36" to R.drawable.ic_money,
        "ic_37" to R.drawable.ic_report,
        "ic_38" to R.drawable.ic_up,
        "ic_39" to R.drawable.ic_down,
        "ic_40" to R.drawable.ic_people,
        "ic_41" to R.drawable.ic_money_income,
        "ic_42" to R.drawable.ic_gift

    )
    fun getIcon(iconName: String): Int {
        return iconMap[iconName] ?: R.drawable.ic_1
    }
    const val ic_1 = "ic_1"
    const val ic_2= "ic_2"
    const val ic_3= "ic_3"
    const val ic_4 = "ic_4"
    const val ic_5 = "ic_5"
    const val ic_6 = "ic_6"
    const val ic_7 = "ic_7"
    const val ic_8 = "ic_8"
    const val ic_9 = "ic_9"
    const val ic_10 = "ic_10"
    const val ic_11 = "ic_11"
    const val ic_12 = "ic_12"
    const val ic_13 = "ic_13"
    const val ic_14 = "ic_14"
    const val ic_15 = "ic_15"
    const val ic_16 = "ic_16"
    const val ic_17 = "ic_17"
    const val ic_18 = "ic_18"
    const val ic_19 = "ic_19"
    const val ic_20 = "ic_20"
    const val ic_21 = "ic_21"
    const val ic_22 = "ic_22"
    const val ic_23 = "ic_23"
    const val ic_24 = "ic_24"
    const val ic_25 = "ic_25"
    const val ic_26 = "ic_26"
    const val ic_27 = "ic_27"
    const val ic_28 = "ic_28"
    const val ic_29 = "ic_29"
    const val ic_30 = "ic_30"
    const val ic_31 = "ic_31"
    const val ic_32 = "ic_32"
    const val ic_33 = "ic_33"
    const val ic_34 = "ic_34"
    const val ic_35 = "ic_35"
    const val ic_36 = "ic_36"
    const val ic_37 = "ic_37"
    const val ic_38 = "ic_38"
    const val ic_39 = "ic_39"
    const val ic_40 = "ic_40"
    const val ic_41 = "ic_41"
    const val ic_42 = "ic_42"
    fun getListIcon() = mutableListOf(ic_1, ic_2, ic_3, ic_4, ic_5, ic_6, ic_7, ic_8, ic_9, ic_10,
        ic_11, ic_12, ic_13, ic_14, ic_15, ic_16, ic_17, ic_18, ic_19, ic_20, ic_21, ic_22, ic_23,
        ic_24, ic_25, ic_26, ic_27, ic_28, ic_29, ic_30, ic_31, ic_32, ic_33, ic_34, ic_35, ic_36,
        ic_37, ic_38, ic_39, ic_40,ic_41, ic_42)
}

