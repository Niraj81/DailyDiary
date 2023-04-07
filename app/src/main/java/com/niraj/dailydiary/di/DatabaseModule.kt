package com.niraj.dailydiary.di

import android.content.Context
import androidx.room.Room
import com.niraj.dailydiary.connectivity.NetworkConnectivityObserver
import com.niraj.dailydiary.data.database.ImagesDatabase
import com.niraj.dailydiary.utils.Constants.IMAGES_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ImagesDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ImagesDatabase::class.java,
            name = IMAGES_DATABASE
        ).build()
    }

    @Provides
    @Singleton
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUpload()

    @Provides
    @Singleton
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDelete()

    @Singleton
    @Provides
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ) = NetworkConnectivityObserver(context)
}