package org.d3if3091.myphone.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.d3if3091.myphone.model.Phone
import org.d3if3091.myphone.network.ApiStatus
import org.d3if3091.myphone.network.PhoneApi
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {
    var data = mutableStateOf(emptyList<Phone>())
        private set
    var status = MutableStateFlow(ApiStatus.LOADING)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = PhoneApi.service.getPhone(userId)
                Log.d("MainViewModel", data.value.toString())
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure:${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun deleteData(email: String, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PhoneApi.service.deletePhone(email, id)
                if (result.status == "success") retrieveData(email)
                else throw Exception(result.message)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failure:${e.message}")
                errorMessage.value = "Error : ${e.message}"
            }
        }
    }

    fun saveData(userId: String, name: String, brand: String, price: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PhoneApi.service.postPhone(
                    userId,
                    name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    brand.toRequestBody("text/plain".toMediaTypeOrNull()),
                    price.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )
                if (result.status == "success") {
                    retrieveData(userId)
                } else throw Exception(result.message)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size
        )
        return MultipartBody.Part.createFormData(
            "image", "image.jpg", requestBody
        )

    }

    fun clearMessage() {
        errorMessage.value = null
    }
}