package com.whatever.raisedragon.applicationservice.goalproof.dto

import com.whatever.raisedragon.domain.gifticon.URL
import com.whatever.raisedragon.domain.goalproof.Comment

data class GoalProofCreateServiceRequest(
    val userId: Long,
    val goalId: Long,
    val url: URL,
    val comment: Comment
)

data class GoalProofUpdateServiceRequest(
    val goalProofId: Long,
    val userId: Long,
    val url: URL? = null,
    val comment: Comment? = null
)