package com.niraj.dailydiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.niraj.dailydiary.navigation.Screen
import com.niraj.dailydiary.navigation.SetupNavGraph
import com.niraj.dailydiary.ui.theme.DailyDiaryTheme
import com.niraj.dailydiary.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyDiaryTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = rememberNavController()
                )
            }
        }
    }
}

private fun getStartDestination() : String {
    val user = App.create(APP_ID).currentUser
    if(user != null && user.loggedIn) return Screen.Home.route
    else return Screen.Authentication.route
}
