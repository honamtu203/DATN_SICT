package com.qltc.finace.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User (
    @PrimaryKey(autoGenerate = true)
    var idUser: Int,
    var name: String,
    var password:String,
    var accountType: String,
    var phone:String?,
    var mail: String?,
) : java.io.Serializable