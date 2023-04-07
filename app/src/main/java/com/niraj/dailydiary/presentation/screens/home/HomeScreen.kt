package com.niraj.dailydiary.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.niraj.dailydiary.R
import com.niraj.dailydiary.data.repository.Diaries
import com.niraj.dailydiary.model.RequestState
import java.time.ZonedDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaries: Diaries,
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    navigateToWrite : () -> Unit,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    navigateToWriteWithArgs : (String) -> Unit,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit
){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllClicked = onDeleteAllClicked
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar (
                    scrollBehavior,
                    onMenuClicked = onMenuClicked,
                    dateIsSelected = dateIsSelected,
                    onDateSelected = onDateSelected,
                    onDateReset = onDateReset
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navigateToWrite ()
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Diary")
                }
            }
        ) {
            when (diaries){
                is RequestState.Success -> {
                    HomeContent(
                        paddingValues = it,
                        diaryNotes = diaries.data,
                        onClick = navigateToWriteWithArgs
                    )
                }
                is RequestState.Error -> {
                    EmptyPage(
                        title = "Error",
                        subtitle = "${diaries.error.message}"
                    )
                }
                is RequestState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    HomeContent(paddingValues = it, diaryNotes = mapOf(), onClick = {})
                }
            }
        }
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    content : @Composable () -> Unit
){
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                 content = {
                     NavigationDrawerItem(
                         label = {
                             Row( Modifier.padding(horizontal = 12.dp)){
                                 Image(
                                     painter = painterResource(id = R.drawable.google_logo),
                                     contentDescription = null
                                 )
                                 Spacer(Modifier.width(12.dp))
                                 Text(text = "Sign Out")
                             }
                         },
                         selected = false,
                         onClick = {
                             onSignOutClicked ()
                         }
                     )
                     NavigationDrawerItem(
                         label = {
                             Row( Modifier.padding(horizontal = 12.dp)){
                                 Icon(
                                     imageVector = Icons.Default.Delete,
                                     tint = MaterialTheme.colorScheme.onSurface,
                                     contentDescription = "Delete Icon"
                                 )
                                 Spacer(Modifier.width(12.dp))
                                 Text(text = "Delete all diaries")
                             }
                         },
                         selected = false,
                         onClick = {
                             onDeleteAllClicked()
                         }
                     )
                 }
            )
        }
    ) {
        content()
    }
}