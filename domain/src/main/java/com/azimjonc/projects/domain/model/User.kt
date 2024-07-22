package com.azimjonc.projects.domain.model

data class User(
    var id: String,
    val phone: String,
    val name: String,
    val avatar: String?
)
