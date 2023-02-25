package com.niraj.dailydiary.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.niraj.dailydiary.presentation.screens.auth.AuthenticationScreen
import com.niraj.dailydiary.presentation.screens.auth.AuthenticationViewModel
import com.niraj.dailydiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import java.lang.Exception

@Composable
fun SetupNavGraph(startDestination : String, navController: NavHostController){
    NavHost(navController = navController, startDestination = startDestination){
        authenticationRoute()
        homeRoute()
        writeRoute()
    }
}

// Creating extension function for NavGraphBuilder
fun NavGraphBuilder.authenticationRoute(){
    composable(route = Screen.Authentication.route){
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val oneTapSignInState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        
        AuthenticationScreen(
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
                    onSuccess = {loggedIn ->
                        if(loggedIn){
                            messageBarState.addSuccess("Successfully Authenticated!")
                            viewModel.setLoading(false)
                        }
                    },
                    onError = {message ->
                        messageBarState.addError(Exception(message))
                    }
                )
            },
            onDialogDismised = {message ->
                messageBarState.addError(Exception(message))
            }
        )
    }
}

fun NavGraphBuilder.homeRoute(){
    composable(route = Screen.Home.route){

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