package com.whatever.raisedragon.applicationservice.goalgifticon

import com.whatever.raisedragon.applicationservice.ApplicationServiceTestSupport
import com.whatever.raisedragon.applicationservice.goalgifticon.dto.GifticonResponse
import com.whatever.raisedragon.applicationservice.goalgifticon.dto.GoalGifticonCreateServiceRequest
import com.whatever.raisedragon.applicationservice.goalgifticon.dto.GoalGifticonResponse
import com.whatever.raisedragon.applicationservice.goalgifticon.dto.GoalGifticonUpdateServiceRequest
import com.whatever.raisedragon.common.exception.BaseException
import com.whatever.raisedragon.domain.gifticon.GifticonEntity
import com.whatever.raisedragon.domain.gifticon.GifticonRepository
import com.whatever.raisedragon.domain.gifticon.URL
import com.whatever.raisedragon.domain.goal.*
import com.whatever.raisedragon.domain.goal.GoalResult.PROCEEDING
import com.whatever.raisedragon.domain.goal.GoalType.BILLING
import com.whatever.raisedragon.domain.goal.GoalType.FREE
import com.whatever.raisedragon.domain.goalgifticon.GoalGifticonEntity
import com.whatever.raisedragon.domain.goalgifticon.GoalGifticonRepository
import com.whatever.raisedragon.domain.user.Nickname
import com.whatever.raisedragon.domain.user.UserEntity
import com.whatever.raisedragon.domain.user.UserRepository
import com.whatever.raisedragon.domain.winner.WinnerEntity
import com.whatever.raisedragon.domain.winner.WinnerRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional
class GoalGifticonApplicationServiceTest : ApplicationServiceTestSupport {

    @Autowired
    private lateinit var goalGifticonApplicationService: GoalGifticonApplicationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var goalRepository: GoalRepository

    @Autowired
    private lateinit var gifticonRepository: GifticonRepository

    @Autowired
    private lateinit var goalGifticonRepository: GoalGifticonRepository

    @Autowired
    private lateinit var winnerRepository: WinnerRepository

    @DisplayName("GoalGifticon을 생성하고 Gifticon을 업로드한다.")
    @Test
    fun createAndUploadGifticon1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val uploadedURL = "www.sample.com/gifticon"
        val request = GoalGifticonCreateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            uploadedURL = uploadedURL
        )

        // when
        val goalGifticonResponse = goalGifticonApplicationService.createAndUploadGifticon(request)

        // then
        assertThat(goalGifticonResponse)
            .isInstanceOf(GoalGifticonResponse::class.java)
        assertThat(goalGifticonResponse.goalId)
            .isEqualTo(goalEntity.id)
        assertThat(goalGifticonResponse.gifticonURL)
            .isEqualTo(uploadedURL)
    }

    @DisplayName("기프티콘을 업로드하고자 하는 다짐이 이미 시작한 경우 GoalGifticon 생성에 실패한다.")
    @Test
    fun createAndUploadGifticon2() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().minusSeconds(1))
        goalRepository.save(goalEntity)

        val uploadedURL = "www.sample.com/gifticon"
        val request = GoalGifticonCreateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            uploadedURL = uploadedURL
        )

        // when // then
        assertThatThrownBy { goalGifticonApplicationService.createAndUploadGifticon(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("기프티콘을 업로드하는 중, 이미 다짐 수행이 시작되어 업로드할 수 없습니다.")
    }

    @DisplayName("기프티콘을 업로드하고자 하는 다짐이 무료다짐일 경우 GoalGifticon 생성에 실패한다.")
    @Test
    fun createAndUploadGifticon3() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, FREE, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val uploadedURL = "www.sample.com/gifticon"
        val request = GoalGifticonCreateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            uploadedURL = uploadedURL
        )

        // when // then
        assertThatThrownBy { goalGifticonApplicationService.createAndUploadGifticon(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("기프티콘을 업르도하는 중, 무료 다짐에는 기프티콘을 업로드할 수 없습니다.")
    }

    @DisplayName("기프티콘을 업로드하고자 하는 유저가 해당 다짐을 생성한 유저가 아니면 GoalGifticon 생성에 실패합니다.")
    @Test
    fun createAndUploadGifticon4() {
        // given
        val userEntity1 = UserEntity(nickname = Nickname("User1"))
        val userEntity2 = UserEntity(nickname = Nickname("User2"))
        userRepository.saveAll(listOf(userEntity1, userEntity2))

        val goalEntity = createGoalEntity(userEntity1, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val uploadedURL = "www.sample.com/gifticon"
        val request = GoalGifticonCreateServiceRequest(
            userId = userEntity2.id,
            goalId = goalEntity.id,
            uploadedURL = uploadedURL
        )

        // when // then
        assertThatThrownBy { goalGifticonApplicationService.createAndUploadGifticon(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("기프티콘을 업로드하는 중, 요청한 유저가 생성한 다짐에 대한 요청이 아닙니다.")
    }

    @DisplayName("해당 다짐의 생성자인 경우 GoalId에 해당하는 GoalGifticon을 조회 할 수 있습니다.")
    @Test
    fun retrieveByGoalId1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val gifticonURL = URL("www.sample.com/gifticon")
        val gifticonEntity = GifticonEntity(userEntity = userEntity, url = gifticonURL)
        gifticonRepository.save(gifticonEntity)

        val goalGifticonEntity = GoalGifticonEntity(goalEntity = goalEntity, gifticonEntity = gifticonEntity)
        goalGifticonRepository.save(goalGifticonEntity)

        // when
        val gifticonResponse = goalGifticonApplicationService.retrieveByGoalId(
            goalId = goalEntity.id,
            userId = userEntity.id
        )

        // then
        assertThat(gifticonResponse).isInstanceOf(GifticonResponse::class.java)
        assertThat(gifticonResponse.goalId).isEqualTo(goalEntity.id)
        assertThat(gifticonResponse.gifticonId).isEqualTo(gifticonEntity.id)
        assertThat(gifticonResponse.gifticonURL).isEqualTo(gifticonEntity.url.value)
    }

    @DisplayName("해당 다짐의 우승자인 경우 GoalId에 해당하는 GoalGifticon을 조회 할 수 있습니다.")
    @Test
    fun retrieveByGoalId2() {
        // given
        val userEntity1 = UserEntity(nickname = Nickname("User1"))
        val userEntity2 = UserEntity(nickname = Nickname("User2"))
        userRepository.saveAll(listOf(userEntity1, userEntity2))

        val goalEntity = createGoalEntity(userEntity1, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val gifticonURL = URL("www.sample.com/gifticon")
        val gifticonEntity = GifticonEntity(userEntity = userEntity1, url = gifticonURL)
        gifticonRepository.save(gifticonEntity)

        val goalGifticonEntity = GoalGifticonEntity(goalEntity = goalEntity, gifticonEntity = gifticonEntity)
        goalGifticonRepository.save(goalGifticonEntity)

        winnerRepository.save(
            WinnerEntity(
                goalEntity = goalEntity,
                userEntity = userEntity2,
                gifticonEntity = gifticonEntity
            )
        )

        // when
        val gifticonResponse = goalGifticonApplicationService.retrieveByGoalId(
            goalId = goalEntity.id,
            userId = userEntity2.id
        )

        // then
        assertThat(gifticonResponse).isInstanceOf(GifticonResponse::class.java)
        assertThat(gifticonResponse.goalId).isEqualTo(goalEntity.id)
        assertThat(gifticonResponse.gifticonId).isEqualTo(gifticonEntity.id)
        assertThat(gifticonResponse.gifticonURL).isEqualTo(gifticonEntity.url.value)
    }

    @DisplayName("해당 다짐의 작성자나 우승자가 아닌경우 GoalId에 해당하는 GoalGifticon을 조회 할 수 없습니다.")
    @Test
    fun retrieveByGoalId3() {
        // given
        val userEntity1 = UserEntity(nickname = Nickname("User1"))
        val userEntity2 = UserEntity(nickname = Nickname("User2"))
        val userEntity3 = UserEntity(nickname = Nickname("User3"))
        userRepository.saveAll(listOf(userEntity1, userEntity2, userEntity3))

        val goalEntity = createGoalEntity(userEntity1, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val gifticonURL = URL("www.sample.com/gifticon")
        val gifticonEntity = GifticonEntity(userEntity = userEntity1, url = gifticonURL)
        gifticonRepository.save(gifticonEntity)

        val goalGifticonEntity = GoalGifticonEntity(goalEntity = goalEntity, gifticonEntity = gifticonEntity)
        goalGifticonRepository.save(goalGifticonEntity)

        winnerRepository.save(
            WinnerEntity(
                goalEntity = goalEntity,
                userEntity = userEntity3,
                gifticonEntity = gifticonEntity
            )
        )

        // when // then
        assertThatThrownBy {
            goalGifticonApplicationService.retrieveByGoalId(
                goalId = goalEntity.id,
                userId = userEntity2.id
            )
        }.isInstanceOf(BaseException::class.java)
            .hasMessage("접근할 수 없는 기프티콘입니다.")
    }

    @DisplayName("조회하고자 하는 Goal의 Gifticon이 등록되어 있지 않은 경우 Gifticon 조회에 실패합니다.")
    @Test
    fun retrieveByGoalId4() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        // when // then
        assertThatThrownBy {
            goalGifticonApplicationService.retrieveByGoalId(
                goalId = goalEntity.id,
                userId = userEntity.id
            )
        }.isInstanceOf(BaseException::class.java)
            .hasMessage("접근할 수 없는 기프티콘입니다.")
    }

    @DisplayName("goalId에 해당하는 다짐의 기프티콘 url를 수정합니다.")
    @Test
    fun updateGifticonURLByGoalId1() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val gifticonURL = URL("www.sample.com/gifticon")
        val gifticonEntity = GifticonEntity(userEntity = userEntity, url = gifticonURL)
        gifticonRepository.save(gifticonEntity)

        val goalGifticonEntity = GoalGifticonEntity(goalEntity = goalEntity, gifticonEntity = gifticonEntity)
        goalGifticonRepository.save(goalGifticonEntity)

        val willChangeGifticonUrl = "www.sample.com/gifticon-updated"
        val request = GoalGifticonUpdateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity.id,
            gifticonURL = willChangeGifticonUrl
        )

        // when
        val goalGifticonResponse = goalGifticonApplicationService.updateGifticonURLByGoalId(request)
        val goalGifticonByRepository = goalGifticonRepository.findByGoalEntity(goalEntity)

        // then
        assertThat(goalGifticonResponse).isInstanceOf(GoalGifticonResponse::class.java)
        assertThat(goalGifticonResponse.goalId).isEqualTo(goalEntity.id)
        assertThat(goalGifticonResponse.gifticonId).isEqualTo(gifticonEntity.id)
        assertThat(goalGifticonResponse.goalGifticonId).isEqualTo(goalGifticonEntity.id)
        assertThat(goalGifticonResponse.gifticonURL).isEqualTo(willChangeGifticonUrl)
        assertThat(goalGifticonResponse.gifticonURL).isEqualTo(goalGifticonByRepository?.gifticonEntity?.url?.value)
    }

    @DisplayName("작성자가 아닌 제 3자가 gifticon url을 수정할 경우 실패합니다.")
    @Test
    fun updateGifticonURLByGoalId2() {
        // given
        val userEntity1 = UserEntity(nickname = Nickname("User1"))
        val userEntity2 = UserEntity(nickname = Nickname("User2"))
        userRepository.saveAll(listOf(userEntity1, userEntity2))

        val goalEntity = createGoalEntity(userEntity1, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.save(goalEntity)

        val gifticonURL = URL("www.sample.com/gifticon")
        val gifticonEntity = GifticonEntity(userEntity = userEntity1, url = gifticonURL)
        gifticonRepository.save(gifticonEntity)

        val goalGifticonEntity = GoalGifticonEntity(goalEntity = goalEntity, gifticonEntity = gifticonEntity)
        goalGifticonRepository.save(goalGifticonEntity)

        val willChangeGifticonUrl = "www.sample.com/gifticon-updated"
        val request = GoalGifticonUpdateServiceRequest(
            userId = userEntity2.id,
            goalId = goalEntity.id,
            gifticonURL = willChangeGifticonUrl
        )

        // when // then
        assertThatThrownBy { goalGifticonApplicationService.updateGifticonURLByGoalId(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("필수 파라미터 값이 없거나 잘못된 값으로 요청을 보낸 경우 발생")
    }

    @DisplayName("작성자가 아닌 제 3자가 gifticon url을 수정할 경우 실패합니다.")
    @Test
    fun updateGifticonURLByGoalId3() {
        // given
        val userEntity = UserEntity(nickname = Nickname("User"))
        userRepository.save(userEntity)

        val goalEntity1 = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        val goalEntity2 = createGoalEntity(userEntity, BILLING, PROCEEDING, LocalDateTime.now().plusDays(1))
        goalRepository.saveAll(listOf(goalEntity1, goalEntity2))

        val gifticonURL = URL("www.sample.com/gifticon")
        val gifticonEntity = GifticonEntity(userEntity = userEntity, url = gifticonURL)
        gifticonRepository.save(gifticonEntity)

        val goalGifticonEntity = GoalGifticonEntity(goalEntity = goalEntity1, gifticonEntity = gifticonEntity)
        goalGifticonRepository.save(goalGifticonEntity)

        val willChangeGifticonUrl = "www.sample.com/gifticon-updated"
        val request = GoalGifticonUpdateServiceRequest(
            userId = userEntity.id,
            goalId = goalEntity2.id,
            gifticonURL = willChangeGifticonUrl
        )

        // when // then
        assertThatThrownBy { goalGifticonApplicationService.updateGifticonURLByGoalId(request) }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("다짐에 등록된 기프티콘을 찾을 수 없습니다.")
    }

    private fun createGoalEntity(
        userEntity: UserEntity,
        goalType: GoalType,
        goalResult: GoalResult,
        startDateTime: LocalDateTime = LocalDateTime.now()
    ): GoalEntity {
        val endDateTime = startDateTime.plusDays(7)
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
