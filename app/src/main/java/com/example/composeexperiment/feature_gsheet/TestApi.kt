package com.example.composeexperiment.feature_gsheet

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface TestApi {

    companion object {
        const val BASE_URL= "https://script.google.com/"
        const val KEY = "AKfycbzC8WkTiSmupicdW1o7X4wZ6OJEPFfDl_UHgGsiZwbd_mpwmSMy2MqVn2WXAG0qJtjq"

        val gson = GsonBuilder()
            .setLenient()
            .create()
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(TestApi::class.java)
        }
    }

    @Headers("Content-Type: application/json")
    @POST("macros/s/$KEY/exec")
    suspend fun exportData(@Query("name") name: String): String


}