package com.whatever.raisedragon.controller.goalproof

import com.whatever.raisedragon.ControllerTestSupport
import com.whatever.raisedragon.applicationservice.goalproof.dto.GoalProofListRetrieveResponse
import com.whatever.raisedragon.applicationservice.goalproof.dto.GoalProofRetrieveResponse
import com.whatever.raisedragon.security.WithCustomUser
import org.hamcrest.core.IsNull.nullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithCustomUser(id = 1L, nickname = "User")
class GoalProofControllerTest : ControllerTestSupport() {

    @DisplayName("GoalProof를 생성한다.")
    @Test
    fun create() {
        // given
        val request = GoalProofCreateRequest(goalId = 1L, url = "www.sample.com", comment = "Sample Comment")

        // when // then
        mockMvc
            .perform(
                post("/v1/goal-proof")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
    }

    @DisplayName("GoalProof를 생성할 때 URL은 공백이 아니다.")
    @Test
    fun createWithBlankUrl() {
        // given
        val request = GoalProofCreateRequest(goalId = 1L, url = "", comment = "Sample Comment")

        // when // then
        mockMvc
            .perform(
                post("/v1/goal-proof")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorResponse.code").value("400"))
            .andExpect(jsonPath("$.errorResponse.detailMessage").value("URL은 공백이어서는 안됩니다."))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("GoalProof를 생성할 때 Comment는 공백이 아니다.")
    @Test
    fun createWithBlankComment() {
        // given
        val request = GoalProofCreateRequest(goalId = 1L, url = "www.sample.com", comment = "")

        // when // then
        mockMvc
            .perform(
                post("/v1/goal-proof")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorResponse.code").value("400"))
            .andExpect(jsonPath("$.errorResponse.detailMessage").value("Comment는 공백이어서는 안됩니다."))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("GoalProof를 조회한다.")
    @Test
    fun retrieve() {
        // given
        val goalProofId = 1L

        // when // then
        mockMvc
            .perform(
                get("/v1/goal-proof/$goalProofId")
            )
            .andDo(::print)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
    }

    @DisplayName("모든 GoalProof를 조회한다.")
    @Test
    fun retrieveAll() {
        // given
        val goalId = 1L
        val mockUserId = 1L
        val goalProofs = listOf<GoalProofRetrieveResponse>()
        val progressDays = listOf<Int>()

        `when`(goalProofApplicationService.retrieveAll(goalId, mockUserId)).thenReturn(
            GoalProofListRetrieveResponse(goalProofs, progressDays)
        )
        // when // then
        mockMvc
            .perform(
                get("/v1/goal/$goalId/goal-proof")
            )
            .andDo(::print)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
            .andExpect(jsonPath("$.data.goalProofs").isArray)
            .andExpect(jsonPath("$.data.progressDays").isArray)
    }

    @DisplayName("해당 다짐이 성공했는지 여부를 조회한다.")
    @Test
    fun isGoalSuccess() {
        // given
        val goalId = 1L

        // when // then
        mockMvc
            .perform(
                get("/v1/goal/$goalId/goal-proof/result")
            )
            .andDo(::print)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
            .andExpect(jsonPath("$.data").isBoolean)
    }

    @DisplayName("GoalProof를 수정한다.")
    @Test
    fun update() {
        // given
        val request = GoalProofUpdateRequest("www.sample.com", "Sample Comment")
        val goalProofId = 1L

        // when // then
        mockMvc
            .perform(
                put("/v1/goal-proof/$goalProofId")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
    }

    @DisplayName("GoalProof를 수정할 때 URL은 공백이 아니다.")
    @Test
    fun updateWithBlankUrl() {
        // given
        val request = GoalProofUpdateRequest(url = "", comment = "Sample Comment")
        val goalProofId = 1L

        // when // then
        mockMvc
            .perform(
                put("/v1/goal-proof/$goalProofId")

                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorResponse.code").value("400"))
            .andExpect(jsonPath("$.errorResponse.detailMessage").value("URL은 공백이어서는 안됩니다."))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("GoalProof를 수정할 때 Comment는 공백이 아니다.")
    @Test
    fun updateWithBlankComment() {
        // given
        val request = GoalProofUpdateRequest(url = "www.sample.com", comment = "")
        val goalProofId = 1L

        // when // then
        mockMvc
            .perform(
                put("/v1/goal-proof/$goalProofId")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorResponse.code").value("400"))
            .andExpect(jsonPath("$.errorResponse.detailMessage").value("Comment는 공백이어서는 안됩니다."))
            .andExpect(jsonPath("$.data").isEmpty())
    }
}