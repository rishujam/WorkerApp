package com.example.composeexperiment.feature_gsheet

import com.google.gson.annotations.SerializedName

data class TestData(
    @SerializedName("name")
    val name: String,
    @SerializedName("clas")
    val clas: String
)