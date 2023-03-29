package com.niraj.dailydiary.data.repository

import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.utils.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllDiaries() : Flow<Diaries>
    fun getSelectedDiary(diaryId: ObjectId) : RequestState<Diary>
    suspend fun addNewDiary(diary: Diary) : RequestState<Diary>

}