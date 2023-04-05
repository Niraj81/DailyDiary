package com.niraj.dailydiary.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.niraj.dailydiary.data.repository.MongoDB
import com.niraj.dailydiary.model.GalleryImage
import com.niraj.dailydiary.model.diary.Mood
import com.niraj.dailydiary.model.rememberGalleryState
import com.niraj.dailydiary.presentation.components.DisplayAlertDialog
import com.niraj.dailydiary.presentation.screens.auth.AuthenticationScreen
import com.niraj.dailydiary.presentation.screens.auth.AuthenticationViewModel
import com.niraj.dailydiary.presentation.screens.home.HomeScreen
import com.niraj.dailydiary.presentation.screens.home.HomeViewModel
import com.niraj.dailydiary.presentation.screens.write.WriteScreen
import com.niraj.dailydiary.presentation.screens.write.WriteViewModel
import com.niraj.dailydiary.utils.Constants.APP_ID
import com.niraj.dailydiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

@Composable
fun SetupNavGraph(startDestination : String, navController: NavHostController){
    NavHost(navController = navController, startDestination = startDestination){
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            navigateToWriteWithArgs = {
                navController.navigate(Screen.Write.passDiaryId(diaryId = it))
            }
        )
        writeRoute(onBackPressed = {
            navController.popBackStack()
        })
    }
}

// Creating extension function for NavGraphBuilder
fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit
){
    composable(route = Screen.Authentication.route){
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val oneTapSignInState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        
        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = loadingState,
            oneTapState = oneTapSignInState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapSignInState.open()
                viewModel.setLoading(true)
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoading(false)
                    },
                    onError = {message ->
                        messageBarState.addError(Exception(message))
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = { e ->
                messageBarState.addError(e)
                viewModel.setLoading(false)
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite : () -> Unit,
    navigateToAuth : () -> Unit,
    navigateToWriteWithArgs : (String) -> Unit
){
    composable(route = Screen.Home.route){
        val viewModel: HomeViewModel = viewModel()
        val diaries by viewModel.diaries
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember  {
            mutableStateOf(false)
        }
        HomeScreen (
            diaries = diaries,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigateToWrite = navigateToWrite,
            drawerState = drawerState,
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWriteWithArgs = navigateToWriteWithArgs
        )

        LaunchedEffect(key1 = Unit){
            MongoDB.configureTheRealm()
        }

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Do you really want to sign out from your Google account?",
            dialogOpened = signOutDialogOpened,
            onDialogClosed = {
                signOutDialogOpened = false
            },
            onYesClicked = {
                scope.launch (Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        navigateToAuth()
                    }
                    App.create(appId = APP_ID).currentUser?.logOut()
                }
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit){
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY){
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ){
        val viewModel: WriteViewModel = viewModel()
        val uiState = viewModel.uiState
        val context = LocalContext.current
        val pagerState = rememberPagerState()
        val galleryState = rememberGalleryState()
        val pageNumber by remember {
            derivedStateOf {
                pagerState.currentPage
            }
        }
        WriteScreen(
            uiState = uiState,
            pagerState = pagerState,
            moodName = {
                Mood.values()[pageNumber].name
            },
            galleryState = galleryState,
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Diary deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    },
                    onError = { error ->
                        Toast.makeText(
                            context,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onBackPressed = onBackPressed,
            onTitleChanged = {
                Log.d("TAG", "Called")
                viewModel.setTitle(it)
            },
            onDescriptionChanged = {
                viewModel.setDescription(it)
            },
            onSaveClicked = { it ->
                viewModel.upsertDiary(
                    it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = {
                        onBackPressed()
                    },
                    onError = { error ->
                        Toast.makeText(
                            context,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onDateTimeUpdated = {
                viewModel.updateDateTime(zonedDateTime = it)
            },
            onImageSelect = {
                galleryState.addImage(
                    GalleryImage(image = it, remoteImagePath = "")
                )
            }
        )
    }
}