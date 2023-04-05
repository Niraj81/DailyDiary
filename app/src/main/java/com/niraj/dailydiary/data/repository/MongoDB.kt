package com.niraj.dailydiary.data.repository

import android.util.Log
import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.utils.Constants.APP_ID
import com.niraj.dailydiary.model.RequestState
import com.niraj.dailydiary.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.ZoneId

object MongoDB : MongoRepository {
    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        Log.d("REALM", "subscription done")
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .name("Diaries")
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Diary>(query = "ownerId == $0", user.id)
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
            Log.d("REALM", config.name)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return if (user != null) {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedDiary(diaryId: ObjectId): Flow<RequestState<Diary>> {
        return if (user != null) {
            try {
                realm.query<Diary>(query = "_id == $0", diaryId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertDiary(diary: Diary): RequestState<Diary> {
        Log.d("ERR", "${diary.title} : ${diary._id} : ${diary.description}")

        return if(user != null){
            realm.write {
                try {
                    this.copyToRealm(
                        diary.apply {
                            ownerId = user.id
                        }
                    )
                    RequestState.Success(data = diary)
                } catch (e : Exception) {
                    RequestState.Error(e)
                }
            }
        }else{
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                val queriedDiary = query<Diary>(query = "_id == $0", diary._id).first().find()
                if(queriedDiary != null){
                    queriedDiary.title = diary.title
                    queriedDiary.description = diary.description
                    queriedDiary.mood = diary.mood
                    queriedDiary.images = diary.images
                    queriedDiary.date = diary.date
                    RequestState.Success(data = queriedDiary)
                }else{
                    RequestState.Error(error = Exception("Queried Diary does not exist."))
                }
            }
        }else{
             RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deletediary(id: ObjectId): RequestState<Diary> {
        return if(user != null){
            realm.write {
                val diary = query<Diary>(query = "_id == $0 AND ownerId == $1", id, user.id)
                    .first().find()
                if(diary != null){
                    try {
                        delete(diary)
                        RequestState.Success(data = diary)
                    } catch (e : Exception){
                        RequestState.Error(e)
                    }
                } else {
                    RequestState.Error(Exception("Diary does not exist."))
                }
            }
        }else{
            RequestState.Error(UserNotAuthenticatedException())
        }
    }
}

private class UserNotAuthenticatedException : Exception("User is not logged in.")