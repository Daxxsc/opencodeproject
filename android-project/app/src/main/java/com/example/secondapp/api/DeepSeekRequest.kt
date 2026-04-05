package com.example.secondapp.api

import com.google.gson.annotations.SerializedName

data class DeepSeekRequest(
    @SerializedName("model")
    val model: String = "deepseek-chat",

    @SerializedName("messages")
    val messages: List<Message>,

    @SerializedName("max_tokens")
    val maxTokens: Int = 1000,

    @SerializedName("temperature")
    val temperature: Double = 0.7,

    @SerializedName("stream")
    val stream: Boolean = false
)

data class Message(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)