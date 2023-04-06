package com.niraj.dailydiary.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.niraj.dailydiary.utils.Constants.IMAGE_TO_UPLOAD_TABLE

@Entity(tableName = IMAGE_TO_UPLOAD_TABLE)
data class ImageToUpload(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteImagePath: String,
    val imageUri: String,
    val sessionUri: String
)
