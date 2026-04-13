package com.nithra.nithraresume.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @POST("smfcm/register.php")
    @FormUrlEncoded
    suspend fun registerFcmToken(
        @Field("register") firstOrUpdate: String,
        @Field("app")      appType: String,
        @Field("email")    androidId: String,
        @Field("regId")    token: String,
        @Field("vname")    versionName: String,
        @Field("vcode")    versionCode: String,
        @Field("andver")   androidVersion: String,
        @Field("sw")       sw: String,
        @Field("asw")      asw: String,
        @Field("w")        widthPixels: String,
        @Field("h")        heightPixels: String,
        @Field("d")        density: String,
        @Field("dn")       buildModel: String,
        @Field("uid")      uid: String
    ): Response<ResponseBody>

    @POST("apps/appfeedback.php")
    @FormUrlEncoded
    suspend fun postFeedback(
        @Field("type")     appType: String,
        @Field("feedback") feedbackText: String,
        @Field("email")    emailId: String,
        @Field("vcode")    appVersionCode: String,
        @Field("model")    deviceModelName: String
    ): Response<ResponseBody>

    @POST("apps/referrer.php")
    @FormUrlEncoded
    suspend fun postReferrer(
        @Field("app") appType: String,
        @Field("ref") source: String,
        @Field("mm")  medium: String,
        @Field("cn")  comp: String,
        @Field("email") emailId: String
    ): Response<ResponseBody>
}
// update 61
