package org.techtown.oneshotphoto

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody

data class TrainingResult(
    @SerializedName("responseCode")
    val responseCode: String
)

data class InferenceResult(
    @SerializedName("responseCode")
    val responseCode: String,
    @SerializedName("base64String")
    val base64String: String
)

data class filterResult(
    @SerializedName("filterList")
    val filterLists: ArrayList<String>
)


