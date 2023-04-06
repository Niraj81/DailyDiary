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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.niraj.dailydiary.data.database.ImageToDeleteDao
import com.niraj.dailydiary.data.database.ImageToUploadDao
import com.niraj.dailydiary.data.database.entity.ImageToUpload
import com.niraj.dailydiary.navigation.Screen
import com.niraj.dailydiary.navigation.SetupNavGraph
import com.niraj.dailydiary.ui.theme.DailyDiaryTheme
import com.niraj.dailydiary.utils.Constants.APP_ID
import com.niraj.dailydiary.utils.retryDeletingImageFromFirebase
import com.niraj.dailydiary.utils.retryUploadingImageToFirebase
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao
    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            DailyDiaryTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = rememberNavController()
                )
            }
        }
        cleanupCheck(scope = lifecycleScope, imageToUploadDao = imageToUploadDao, imageToDeleteDao = imageToDeleteDao)
    }
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
) {
    scope.launch (Dispatchers.IO) {
        val result = imageToUploadDao.getAllImages()
        result.forEach {imageToUpload ->
            retryUploadingImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch (Dispatchers.IO){
                        imageToUploadDao.cleanupImage(imageId = imageToUpload.id)
                    }
                }
            )
        }
        val result2 = imageToDeleteDao.getAllImages()
        result2.forEach {
            retryDeletingImageFromFirebase(
                it,
                onSuccess = {
                    scope.launch (Dispatchers.IO){
                        imageToDeleteDao.cleanupImage(imageId = it.id)
                    }
                }
            )
        }
    }
}

private fun getStartDestination() : String {
    val user = App.create(appId =  APP_ID).currentUser
    if(user != null && user.loggedIn) return Screen.Home.route
    else return Screen.Authentication.route
}
