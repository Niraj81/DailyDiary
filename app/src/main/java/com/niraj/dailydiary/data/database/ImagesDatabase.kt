package com.niraj.dailydiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niraj.dailydiary.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class],
    version = 1,
    exportSchema = false
)
abstract class ImagesDatabase: RoomDatabase(){
    abstract fun imageToUpload(): ImageToUploadDao
}