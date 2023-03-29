package com.niraj.dailydiary.presentation.screens.write

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niraj.dailydiary.data.repository.Diaries
import com.niraj.dailydiary.data.repository.MongoDB
import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.model.diary.Mood
import com.niraj.dailydiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.niraj.dailydiary.utils.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState = mutableStateOf(UiState())
    private set

    init {
        getDiaryArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryArgument() {
        uiState.value = uiState.value.copy(
            selectedDiaryId =   savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary() {
        if (uiState.value.selectedDiaryId != null){
            viewModelScope.launch (Dispatchers.Main){
                val diary = MongoDB.getSelectedDiary(
                    ObjectId.from(uiState.value.selectedDiaryId!!)
                )
                if (diary is RequestState.Success){
                    setSelectedDiary(diary = diary.data)
                    setTitle(diary.data.title)
                    setDescription(diary.data.description)
                    setModd(Mood.valueOf(diary.data.mood))
                }
            }
        }
    }

    fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = MongoDB.addNewDiary(diary = diary)
            if(result is RequestState.Success){
                withContext(Dispatchers.Main){
                    onSuccess()
                }
            } else if (result is RequestState.Error) {
                withContext(Dispatchers.Main){
                    onError(result.error.message.toString())
                }
            }
        }
    }

    fun setSelectedDiary(diary: Diary) {
        uiState.value.selectedDiary = diary
    }

    fun setTitle(title: String){
        uiState.value.title = title
    }

    fun setDescription(description: String){
        uiState.value.description = description
    }

    fun setModd(mood:Mood){
        uiState.value.mood = mood
    }
}

data class UiState(
    val selectedDiaryId: String? = null,
    var selectedDiary: Diary? = null,
    var title: String = "",
    var description: String = "",
    var mood: Mood = Mood.Neutral
)