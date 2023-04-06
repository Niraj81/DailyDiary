package com.niraj.dailydiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niraj.dailydiary.data.database.entity.ImageToDelete
import com.niraj.dailydiary.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase: RoomDatabase(){
    abstract fun imageToUpload(): ImageToUploadDao
    abstract fun imageToDelete(): ImageToUploadDao
}