package com.whatever.raisedragon.applicationservice.auth

import com.whatever.raisedragon.applicationservice.auth.dto.LoginResponse
import com.whatever.raisedragon.applicationservice.auth.dto.LoginServiceRequest
import com.whatever.raisedragon.applicationservice.auth.dto.TokenRefreshResponse
import com.whatever.raisedragon.common.exception.BaseException
import com.whatever.raisedragon.common.exception.ExceptionCode
import com.whatever.raisedragon.domain.refreshtoken.RefreshTokenService
import com.whatever.raisedragon.domain.user.Nickname
import com.whatever.raisedragon.domain.user.User
import com.whatever.raisedragon.domain.user.UserService
import com.whatever.raisedragon.external.oauth.AuthService
import com.whatever.raisedragon.security.jwt.JwtAgent
import com.whatever.raisedragon.security.jwt.JwtToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthApplicationService(
    private val authService: AuthService,
    private val userService: UserService,
    private val refreshTokenService: RefreshTokenService,
    private val jwtAgent: JwtAgent,
) {

    @Transactional
    fun kakaoLogin(request: LoginServiceRequest): LoginResponse {
        val kakaoId = authService.verifyKaKao(request.accessToken)
        val user = userService.findByOAuthPayload(kakaoId)

        if (user == null) {
            val newUser = userService.create(
                oauthTokenPayload = kakaoId,
                fcmTokenPayload = null,
                nickname = Nickname.generateRandomNickname()
            )
            return buildLoginResponseByNewUser(newUser)
        }

        if (user.deletedAt != null) {
            userService.convertBySoftDeleteToEntity(user.id!!)
        }

        return buildLoginResponseByUser(user)
    }

    private fun buildLoginResponseByNewUser(newUser: User): LoginResponse {
        val jwtToken = jwtAgent.provide(newUser)
        newUser.id ?: throw BaseException.of(ExceptionCode.E500_INTERNAL_SERVER_ERROR)

        val refreshToken = refreshTokenService.create(newUser.id!!, jwtToken.refreshToken).payload
            ?: throw BaseException.of(ExceptionCode.E400_BAD_REQUEST)

        return LoginResponse(
            userId = newUser.id!!,
            nickname = newUser.nickname.value,
            accessToken = jwtToken.accessToken,
            refreshToken = refreshToken,
            nicknameIsModified = newUser.createdAt!! < newUser.updatedAt
        )
    }

    private fun buildLoginResponseByUser(user: User): LoginResponse {
        user.id ?: throw BaseException.of(ExceptionCode.E500_INTERNAL_SERVER_ERROR)
        val jwtToken = JwtToken(
            accessToken = jwtAgent.provide(user).accessToken,
            refreshToken = refreshTokenService.findByUserId(user.id!!)?.payload!!
        )

        return LoginResponse(
            userId = user.id!!,
            nickname = user.nickname.value,
            accessToken = jwtToken.accessToken,
            refreshToken = jwtToken.refreshToken,
            nicknameIsModified = user.createdAt!! < user.updatedAt
        )
    }

    @Transactional
    fun reissueToken(refreshToken: String): TokenRefreshResponse {
        val refreshTokenVo = refreshTokenService.findByPayload(refreshToken) ?: throw BaseException.of(
            exceptionCode = ExceptionCode.E400_BAD_REQUEST,
            executionMessage = "잘못된 토큰으로 요청하셨습니다."
        )

        val userId = refreshTokenVo.userId
        val user = userService.findById(userId)
        val jwtToken = jwtAgent.reissueToken(refreshToken, user)

        refreshTokenService.updatePayloadByUserId(userId, jwtToken.refreshToken)

        return TokenRefreshResponse(
            accessToken = jwtToken.accessToken,
            refreshToken = jwtToken.refreshToken
        )
    }
}
