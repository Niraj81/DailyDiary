package com.niraj.dailydiary.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.model.diary.Mood

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WriteScreen(
    uiState: MutableState<UiState>,
    pagerState: PagerState,
    moodName: () -> String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged : (String) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
    onSaveClicked : (Diary) -> Unit
) {
    LaunchedEffect(key1 = uiState.value.mood){
        pagerState.scrollToPage(Mood.valueOf(uiState.value.mood.name).ordinal)
    }
    Scaffold(
        topBar = {
            WriteTopBar(
                moodName = moodName,
                selectedDiary = uiState.value.selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed
            )
        },
        content = {
            WriteContent(
                uiState = uiState,
                paddingValues = it,
                pagerState = pagerState,
                title = uiState.value.title,
                onTitleChanged = onTitleChanged,
                description = uiState.value.description,
                onDescriptionChanged = onDescriptionChanged,
                onSaveClicked = onSaveClicked
            )
        }
    )
}