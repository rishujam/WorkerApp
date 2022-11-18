package com.example.composeexperiment.feature_gsheet

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TestApi {

    companion object {
        const val BASE_URL= "https://script.google.com/"
        const val KEY = "AKfycbwwDj6LuOx-vF0OEqhVqrrWHj-xdvuWFS25n8_HiuMKnoXCmDNOxS20H_dPHh-MkRXg"
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
    suspend fun exportData(@Body testData: TestData): String
}