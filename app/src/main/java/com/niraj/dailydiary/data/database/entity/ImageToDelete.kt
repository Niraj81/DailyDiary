package com.niraj.dailydiary.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.niraj.dailydiary.utils.Constants.IMAGE_TO_DELETE_TABLE

@Entity(tableName = IMAGE_TO_DELETE_TABLE)
data class ImageToDelete(
    @PrimaryKey
    val id: Int = 0,
    val remoteImagePath: String
)
