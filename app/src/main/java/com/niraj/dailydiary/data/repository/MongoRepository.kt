package com.niraj.dailydiary.data.repository

import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.utils.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllDiaries() : Flow<Diaries>
}