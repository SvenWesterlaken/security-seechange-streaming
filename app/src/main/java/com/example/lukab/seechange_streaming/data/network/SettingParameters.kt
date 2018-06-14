package com.example.lukab.seechange_streaming.data.network

private const val baseUrl: String = "/api"
private const val userUrl: String = "$baseUrl/user"

const val updatePublicNameUrl: String = "$userUrl/publicname"
const val updateSloganUrl: String = "$userUrl/slogan"
const val getUserInfoUrl: String = "$userUrl/info"
const val avatarUrl: String = "$userUrl/avatar"