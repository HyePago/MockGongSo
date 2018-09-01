package com.emirim.hyejin.mokgongso.model

import com.google.gson.annotations.SerializedName

data class Mandalart (
        @SerializedName("title") var title: String,
        @SerializedName("subMandalartTitle") var subMandalartTitle: Array<String>,
        @SerializedName("thirdContent") var thirdContent: Array<Array<String>>
);