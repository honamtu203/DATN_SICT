package com.qltc.finace.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["idUser", "idCategory"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["idUser"],
            childColumns = ["idUser"]
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["idCategory"],
            childColumns = ["idCategory"]
        ),
    ]
)
data class User_Category (
    @ColumnInfo("idUser")
    var idUser: Int,
    @ColumnInfo("idCategory")
    var idCategory: Int,
)