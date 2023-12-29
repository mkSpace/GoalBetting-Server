package com.whatever.raisedragon.applicationservice

import com.whatever.raisedragon.common.exception.BaseException
import com.whatever.raisedragon.common.exception.ExceptionCode
import com.whatever.raisedragon.controller.user.UserNicknameDuplicatedResponse
import com.whatever.raisedragon.controller.user.UserRetrieveResponse
import com.whatever.raisedragon.domain.betting.BettingService
import com.whatever.raisedragon.domain.gifticon.GifticonService
import com.whatever.raisedragon.domain.goal.GoalService
import com.whatever.raisedragon.domain.goalproof.GoalProofService
import com.whatever.raisedragon.domain.refreshtoken.RefreshTokenService
import com.whatever.raisedragon.domain.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserApplicationService(
    private val userService: UserService,
    private val refreshTokenService: RefreshTokenService,
    private val goalService: GoalService,
    private val goalProofService: GoalProofService,
    private val gifticonService: GifticonService,
    private val bettingService: BettingService,
) {

    fun retrieve(id: Long): UserRetrieveResponse {
        val user = userService.loadById(id)
        return UserRetrieveResponse(
            userId = user.id!!,
            nickname = user.nickname,
            nicknameIsModified = user.createdAt!! < user.updatedAt
        )
    }

    @Transactional
    fun updateNickname(id: Long, nickname: String): UserRetrieveResponse {
        val user = userService.updateNickname(id, nickname)
        return UserRetrieveResponse(
            userId = user.id!!,
            nickname = user.nickname,
            nicknameIsModified = user.createdAt!! < user.updatedAt
        )
    }

    @Transactional
    fun delete(id: Long) {
        val user = userService.loadById(id)
        if (goalService.findProceedingGoalIsExistsByUser(user)) {
            throw BaseException.of(
                exceptionCode = ExceptionCode.E400_BAD_REQUEST,
                executionMessage = "아직 진행중인 다짐이 있어 회원탈퇴에 실패했습니다."
            )
        }
        userService.hardDelete(id)
        refreshTokenService.hardDelete(user)
        goalService.hardDelete(user)
        goalProofService.hardDelete(user)
        gifticonService.hardDelete(user)
        bettingService.hardDelete(user)
    }

    fun isNicknameDuplicated(
        nickname: String
    ): UserNicknameDuplicatedResponse {
        return UserNicknameDuplicatedResponse(
            nicknameIsDuplicated = userService.isNicknameDuplicated(nickname)
        )
    }
}