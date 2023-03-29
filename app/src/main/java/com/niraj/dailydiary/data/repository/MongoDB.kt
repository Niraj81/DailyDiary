package com.niraj.dailydiary.data.repository

import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.utils.Constants.APP_ID
import com.niraj.dailydiary.utils.RequestState
import com.niraj.dailydiary.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.net.UnknownServiceException
import java.time.ZoneId

object MongoDB : MongoRepository {
    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (user == null) return
        val config = SyncConfiguration.Builder(user, setOf(Diary::class))
            .initialSubscriptions {sub ->
                add(
                    query = sub.query<Diary>("ownerID == $0", user.identity),
                    name = "User's Diaries"
                )
            }
            .log(LogLevel.ALL)
            .build()
        realm = Realm.open(config)
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return if(user != null) {
            try {
                realm.query<Diary> (
                    query = "ownerId == $0",
                    user.identity
                )
                    .sort(property = "date")
                    .asFlow()
                    .map { result ->
                        RequestState.Success (
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                }
            }
            catch (e : Exception){
                flow {
                    emit(RequestState.Error(e))
                }
            }
        } else {
            flow {
                emit(RequestState.Error(UserNotAuthenticatedException()))
            }
        }

    }

    override fun getSelectedDiary(diaryId: ObjectId): RequestState<Diary> {
        return if(user != null){
            try {
                val diary = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                RequestState.Success(data = diary)
            } catch (e : Exception) {
                RequestState.Error(e)
            }
        }else{
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun addNewDiary(diary: Diary): RequestState<Diary> {
        return if(user != null){
            realm.write {
                try {
                    val addedDiary = copyToRealm(
                        diary.apply {
                            ownerId = user.identity
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
}

private class UserNotAuthenticatedException : Exception("User is not logged in.")