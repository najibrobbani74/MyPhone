package org.d3if3091.myphone.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Phone(
    @Json(name = "id") val id:String,
    @Json(name = "name") val name:String,
    @Json(name = "brand") val brand:String,
    @Json(name = "price") val price:String,
    @Json(name = "image") val image:String,
)
@JsonClass(generateAdapter = true)
data class PhoneList(val products: List<Phone>)