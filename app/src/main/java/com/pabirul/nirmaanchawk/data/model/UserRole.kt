package com.pabirul.nirmaanchawk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("laborer")
    LABORER,
    @SerialName("contractor")
    CONTRACTOR,
    @SerialName("client")
    CLIENT
}
