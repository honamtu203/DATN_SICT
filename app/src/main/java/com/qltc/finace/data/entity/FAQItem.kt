package com.qltc.finace.data.entity

data class FAQItem(
    val id: Int,
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
) 