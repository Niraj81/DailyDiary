package com.niraj.dailydiary.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niraj.dailydiary.data.repository.Diaries
import com.niraj.dailydiary.data.repository.MongoDB
import com.niraj.dailydiary.utils.RequestState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var diaries : MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {

    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            MongoDB.getAllDiaries().collect() { result ->
                diaries.value = result
            }
        }
    }
}