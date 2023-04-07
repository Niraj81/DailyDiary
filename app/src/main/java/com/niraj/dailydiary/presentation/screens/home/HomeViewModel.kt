package com.niraj.dailydiary.presentation.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.niraj.dailydiary.connectivity.ConnectivityObserver
import com.niraj.dailydiary.connectivity.NetworkConnectivityObserver
import com.niraj.dailydiary.data.database.ImageToDeleteDao
import com.niraj.dailydiary.data.database.entity.ImageToDelete
import com.niraj.dailydiary.data.repository.Diaries
import com.niraj.dailydiary.data.repository.MongoDB
import com.niraj.dailydiary.model.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
): ViewModel() {

    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)
    var diaries : MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
        viewModelScope.launch {
            connectivity.observe().collect {
                network = it
            }
        }
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            MongoDB.getAllDiaries().collect() { result ->
                Log.d("RET", result.toString())
                diaries.value = result
            }
        }
    }

   fun deleteAllDiaries(
       onSuccess: () -> Unit,
       onError: (Throwable) -> Unit
   ) {
        if(network == ConnectivityObserver.Status.Available){
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imageDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imageDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach {ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch (Dispatchers.IO) {
                                    imageToDeleteDao.addImageToDelete(
                                        ImageToDelete(
                                            remoteImagePath = imagePath
                                        )
                                    )
                                }
                            }
                    }
                }
                .addOnFailureListener {onError(it)}
            viewModelScope.launch (Dispatchers.IO) {
                val result = MongoDB.deleteAllDiaries()
                if(result is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }else if(result is RequestState.Error){
                    withContext(Dispatchers.Main) {
                        onError(result.error)
                    }
                }
            }
        }
       else {
           onError(Exception("No Internet Connection"))
        }
   }
}