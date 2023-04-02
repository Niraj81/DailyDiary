package com.niraj.dailydiary.presentation.screens.write

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niraj.dailydiary.data.repository.MongoDB
import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.model.diary.Mood
import com.niraj.dailydiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.niraj.dailydiary.utils.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
    private set

    init {
        getDiaryArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary() {
        if (uiState.selectedDiaryId != null){
            viewModelScope.launch {
                MongoDB.getSelectedDiary(diaryId = ObjectId.invoke(uiState.selectedDiaryId!!))
                    .catch{
                        emit(
                            RequestState.Error(Exception("Diary is already deleted."))
                        )
                    }
                    .collect { diary ->
                        if (diary is RequestState.Success) {
                            setMood(mood = Mood.valueOf(diary.data.mood))
                            setSelectedDiary(diary = diary.data)
                            setTitle(title = diary.data.title)
                            setDescription(description = diary.data.description)
                        }
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
    private fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }
    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

}

data class UiState(
    val selectedDiaryId: String? = null,
    var selectedDiary: Diary? = null,
    var title: String = "",
    var description: String = "",
    var mood: Mood = Mood.Neutral
)