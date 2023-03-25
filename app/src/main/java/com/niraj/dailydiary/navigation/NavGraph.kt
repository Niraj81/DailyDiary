package com.niraj.dailydiary.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.niraj.dailydiary.data.repository.MongoDB
import com.niraj.dailydiary.presentation.components.DisplayAlertDialog
import com.niraj.dailydiary.presentation.screens.auth.AuthenticationScreen
import com.niraj.dailydiary.presentation.screens.auth.AuthenticationViewModel
import com.niraj.dailydiary.presentation.screens.home.HomeScreen
import com.niraj.dailydiary.presentation.screens.home.HomeViewModel
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
            }
        )
        writeRoute()
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
            onTokenReceived = {tokenId ->
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
            onDialogDismised = {message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite : () -> Unit,
    navigateToAuth : () -> Unit
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
            }
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
                    withContext(Dispatchers.Main){
                        navigateToAuth()
                    }
                    val user = App.create(appId = APP_ID).currentUser
                    if(user != null){
                        user.logOut()
                    }
                }
            }
        )
    }
}

fun NavGraphBuilder.writeRoute(){
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY){
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ){

    }
}