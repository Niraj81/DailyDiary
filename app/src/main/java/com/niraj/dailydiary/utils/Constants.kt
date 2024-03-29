package com.niraj.dailydiary.utils

import com.niraj.dailydiary.BuildConfig

object Constants {
    const val APP_ID = BuildConfig.APP_ID
    const val CLIENT_ID = BuildConfig.CLIENT_ID
    const val WRITE_SCREEN_ARGUMENT_KEY = "diaryId"

    const val IMAGES_DATABASE = "images_db"
    const val IMAGE_TO_UPLOAD_TABLE = "images_to_upload_table"
    const val IMAGE_TO_DELETE_TABLE = "images_to_delete_table"
}