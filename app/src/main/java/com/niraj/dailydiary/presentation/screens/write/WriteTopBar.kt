package com.niraj.dailydiary.presentation.screens.write

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.niraj.dailydiary.model.diary.Diary
import com.niraj.dailydiary.presentation.components.DisplayAlertDialog
import com.niraj.dailydiary.utils.toInstant
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopBar(
    selectedDiary: Diary?,
    moodName : () -> String,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit
){

    val currentDate = remember { mutableStateOf(LocalDate.now()) }
    val currentTime = remember { mutableStateOf(LocalTime.now()) }

    val formattedDate = remember(key1 = currentDate) {
        DateTimeFormatter
            .ofPattern("dd MMM yyyy")
            .format(currentDate.value).uppercase()
    }
    val formattedTime = remember(key1 = currentTime) {
        DateTimeFormatter
            .ofPattern("hh:mm a")
            .format(currentTime.value).uppercase()
    }

    val selectedDiaryDateTime = remember(selectedDiary) {
        if(selectedDiary != null){
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date.from(selectedDiary.date.toInstant())).uppercase()
        }else{
            "Unknow"
        }
    }
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go Back"
                )
            }
        },
        title = {
            Column {
                Text(
                    moodName(),
                    Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if(selectedDiary != null) selectedDiaryDateTime else "${formattedDate}, ${formattedTime}",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            if(selectedDiary != null){
                DeleteDiaryAction(
                    selectedDiary = selectedDiary,
                    onDeleteConfirmed = onDeleteConfirmed
                )
            }
        }
    )
}
@Composable
fun DeleteDiaryAction(
    selectedDiary: Diary?,
    onDeleteConfirmed: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(text = "Delete")
            }, onClick = {
                openDialog.value = true
                expanded.value = false
            }
        )
    }
    DisplayAlertDialog(
        title = "Delete",
        message = "Are you sure you want to permanently delete this diary note '${selectedDiary?.title}'?",
        dialogOpened = openDialog.value,
        onDialogClosed = { openDialog.value = false },
        onYesClicked = onDeleteConfirmed
    )
    IconButton(onClick = { expanded.value = !expanded.value }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Overflow Menu Icon",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}