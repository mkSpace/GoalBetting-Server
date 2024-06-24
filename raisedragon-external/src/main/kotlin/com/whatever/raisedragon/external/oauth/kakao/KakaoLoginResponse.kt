package com.whatever.raisedragon.external.oauth.kakao

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class KakaoLoginResponse(
    @JsonProperty("id")
    val id: String? = null,

    @JsonProperty("connected_at")
    val connectedAt: LocalDateTime? = null,
)
