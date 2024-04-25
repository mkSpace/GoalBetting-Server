package com.whatever.raisedragon.applicationservice.goalproof

import com.whatever.raisedragon.applicationservice.ApplicationServiceTestSupport
import com.whatever.raisedragon.applicationservice.goalproof.dto.GoalProofCreateServiceRequest
import com.whatever.raisedragon.applicationservice.goalproof.dto.GoalProofRetrieveResponse
import com.whatever.raisedragon.applicationservice.goalproof.dto.GoalProofUpdateServiceRequest
import com.whatever.raisedragon.common.exception.BaseException
import com.whatever.raisedragon.domain.gifticon.URL
import com.whatever.raisedragon.domain.goal.*
import com.whatever.raisedragon.domain.goalproof.Comment
import com.whatever.raisedragon.domain.goalproof.GoalProofEntity
import com.whatever.raisedragon.domain.goalproof.GoalProofRepository
import com.whatever.raisedragon.domain.user.Nickname
import com.whatever.raisedragon.domain.user.UserEntity
import com.whatever.raisedragon.domain.user.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Transactional
class GoalProofApplicationServiceTest : ApplicationServiceTestSupport {

    @Autowired
    private lateinit var goalProofApplicationService: GoalProofApplicationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var goalRepository: GoalRepository

    @Autowired
    private lateinit var goalProofRepository: GoalProofRepository

    @DisplayName("다짐 인증을 생성할 수 있다.")
    @Test
    fun create1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val request = GoalProofCreateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            url = goalProofImageUrl,
            comment = comment
        )
        // when
        val response = goalProofApplicationService.create(request)

        // then
        assertThat(response)
            .isInstanceOf(GoalProofRetrieveResponse::class.java)
        assertThat(response.goalId).isEqualTo(goalEntity.id)
        assertThat(response.userId).isEqualTo(userEntity.id)
        assertThat(response.url).isEqualTo(goalProofImageUrl)
        assertThat(response.comment).isEqualTo(comment)
    }

    @DisplayName("같은 날짜에 대한 인증이 이미 생성되어있다면 다짐 인증을 생성할 수 없다.")
    @Test
    fun create2() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofEntity = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = URL("Sample"),
            comment = Comment("comment")
        )
        goalProofRepository.save(goalProofEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val request = GoalProofCreateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            url = goalProofImageUrl,
            comment = comment
        )
        // when // then
        assertThatThrownBy { goalProofApplicationService.create(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("해당 날짜에 대한 인증은 이미 생성되어있습니다.")
    }

    @DisplayName("인증 날짜가 다짐을 시작한 후 7일 이내가 아닌 경우 다짐 인증 생성에 실패한다.")
    @Test
    fun create3() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(7))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val request = GoalProofCreateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            url = goalProofImageUrl,
            comment = comment
        )
        // when // then
        assertThatThrownBy { goalProofApplicationService.create(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("인증 날짜가 올바르지 않습니다.")
    }

    @DisplayName("다짐 인증을 단건 조회한다.")
    @Test
    fun retrieve1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.save(goalProofEntity)

        // when
        val response = goalProofApplicationService.retrieve(goalProofEntity.id)

        // then
        assertThat(response).isInstanceOf(GoalProofRetrieveResponse::class.java)
        assertThat(response.id).isEqualTo(goalProofEntity.id)
        assertThat(response.goalId).isEqualTo(goalEntity.id)
        assertThat(response.userId).isEqualTo(userEntity.id)
        assertThat(response.url).isEqualTo(goalProofImageUrl)
        assertThat(response.comment).isEqualTo(comment)
    }

    @DisplayName("다짐 인증 단건 조회 시 해당하지 않은 id로 조회할 경우 조회에 실패한다.")
    @Test
    fun retrieve2() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.save(goalProofEntity)

        // when // then
        assertThatThrownBy { goalProofApplicationService.retrieve(-1L) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("요청한 리소스가 존재하지 않는 경우 발생")
    }

    @DisplayName("다짐 인증이 총 7일 모두 인증 되었다면 true를 반환한다.")
    @Test
    fun isSuccess1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity1 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity2 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity3 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity4 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity5 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity6 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity7 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.saveAll(
            listOf(
                goalProofEntity1,
                goalProofEntity2,
                goalProofEntity3,
                goalProofEntity4,
                goalProofEntity5,
                goalProofEntity6,
                goalProofEntity7
            )
        )

        // when
        val result = goalProofApplicationService.isSuccess(goalEntity.id, userEntity.id)

        // then
        assertThat(result).isTrue()
    }

    @DisplayName("다짐 인증이 총 7일 모두 인증 되지 않았다면 false를 반환한다.")
    @Test
    fun isSuccess2() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity1 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        val goalProofEntity2 = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.saveAll(
            listOf(
                goalProofEntity1,
                goalProofEntity2
            )
        )

        // when
        val result = goalProofApplicationService.isSuccess(goalEntity.id, userEntity.id)

        // then
        assertThat(result).isFalse()
    }

    @DisplayName("다짐 인증 내역으르 수정할 수 있다.")
    @Test
    fun update1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.save(goalProofEntity)

        val willChangeUrl = URL("www.sample.com/changed-url")
        val willChangeComment = Comment("changed-sample-comment")
        val request = GoalProofUpdateServiceRequest(
            goalProofId = goalProofEntity.id,
            userId = userEntity.id,
            url = willChangeUrl,
            comment = willChangeComment
        )

        // when
        val response = goalProofApplicationService.update(request)

        // then
        val savedGoalProof = goalProofRepository.findById(goalProofEntity.id).getOrNull()
        assertThat(response).isInstanceOf(GoalProofRetrieveResponse::class.java)
        assertThat(response.id).isEqualTo(goalProofEntity.id).isEqualTo(savedGoalProof?.id)
        assertThat(response.goalId).isEqualTo(goalEntity.id).isEqualTo(savedGoalProof?.goalEntity?.id)
        assertThat(response.userId).isEqualTo(userEntity.id).isEqualTo(savedGoalProof?.userEntity?.id)
        assertThat(response.url).isEqualTo(willChangeUrl.value).isEqualTo(savedGoalProof?.url?.value)
        assertThat(response.comment).isEqualTo(willChangeComment.value).isEqualTo(savedGoalProof?.comment?.value)
    }

    @DisplayName("다짐 인증 내역을 본인이 아닌 유저가 요청한 경우 수정에 실패한다.")
    @Test
    fun update2() {
        // given
        val userEntity1 = UserEntity(nickname = Nickname("User1"))
        val userEntity2 = UserEntity(nickname = Nickname("User2"))
        userRepository.saveAll(listOf(userEntity1, userEntity2))

        val goalEntity =
            createGoalEntity(userEntity1, GoalType.BILLING, GoalResult.PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity = GoalProofEntity(
            userEntity = userEntity1,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.save(goalProofEntity)

        val willChangeUrl = URL("www.sample.com/changed-url")
        val willChangeComment = Comment("changed-sample-comment")
        val request = GoalProofUpdateServiceRequest(
            goalProofId = goalProofEntity.id,
            userId = userEntity2.id,
            url = willChangeUrl,
            comment = willChangeComment
        )

        // when // then
        assertThatThrownBy { goalProofApplicationService.update(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("접근할 수 없는 다짐 인증입니다")
    }

    @DisplayName("이미 끝난 다짐의 경우 다짐 인증 수정에 실패한다.")
    @Test
    fun update3() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User1"))
        userRepository.save(userEntity)

        val now = LocalDateTime.now()
        val goalEntity =
            createGoalEntity(userEntity, GoalType.BILLING, GoalResult.PROCEEDING, now.minusDays(7))
        goalRepository.save(goalEntity)

        val goalProofImageUrl = URL("www.sample.com/goal-proof-image.png")
        val comment = Comment("Sample comment")

        val goalProofEntity = GoalProofEntity(
            userEntity = userEntity,
            goalEntity = goalEntity,
            url = goalProofImageUrl,
            comment = comment
        )
        goalProofRepository.save(goalProofEntity)

        val willChangeUrl = URL("www.sample.com/changed-url")
        val willChangeComment = Comment("changed-sample-comment")
        val request = GoalProofUpdateServiceRequest(
            goalProofId = goalProofEntity.id,
            userId = userEntity.id,
            url = willChangeUrl,
            comment = willChangeComment
        )

        // when // then
        assertThatThrownBy { goalProofApplicationService.update(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("이미 끝난 내기입니다")
    }

    private fun createGoalEntity(
        userEntity: UserEntity,
        goalType: GoalType,
        goalResult: GoalResult,
        startDateTime: LocalDateTime = LocalDateTime.now(),
        endDateTime: LocalDateTime = startDateTime.plusDays(7)
    ): GoalEntity {
        return GoalEntity(
            userEntity = userEntity,
            goalType = goalType,
            content = Content("sampleContent"),
            goalResult = goalResult,
            startDate = startDateTime,
            endDate = endDateTime
        )
    }
}