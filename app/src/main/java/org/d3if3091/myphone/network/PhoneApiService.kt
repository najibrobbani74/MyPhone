package org.d3if3091.myphone.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.d3if3091.myphone.model.OpStatus
import org.d3if3091.myphone.model.Phone
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

private const val BASE_URL = "https://4602myproductapi.000webhostapp.com/"
private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private val retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshi)).baseUrl(
    BASE_URL).build()
interface PhoneApiService {
    @GET("6706223091/product")
    suspend fun getPhone(
        @Header("Authorization") userId:String
    ):List<Phone>
    @GET("6706223091/product/delete/{id}")
    suspend fun deletePhone(
        @Header("Authorization") userId:String,
        @Path("id") id:String
    ): OpStatus

    @Multipart
    @POST("6706223091/product")
    suspend fun postPhone(
        @Header("Authorization") userId:String,
        @Part("name") nama: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("price") price: RequestBody,
        @Part image: MultipartBody.Part,
    ): OpStatus
}
object PhoneApi {
    val service: PhoneApiService by lazy {
        retrofit.create(PhoneApiService::class.java)
    }

    fun getPhoneUrl(imageId:String):String{
        return "${BASE_URL}images/$imageId"
    }
}

enum class ApiStatus {LOADING,SUCCESS,FAILED}