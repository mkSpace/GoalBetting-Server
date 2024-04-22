package com.whatever.raisedragon.controller.goalgifticon

import com.whatever.raisedragon.ControllerTestSupport
import com.whatever.raisedragon.security.WithCustomUser
import org.hamcrest.core.IsNull.nullValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithCustomUser(id = 1L, nickname = "User")
class GoalGifticonControllerTest : ControllerTestSupport() {

    @DisplayName("GoalGifticon을 생성한다.")
    @Test
    fun create() {
        // given
        val gifticonUrl = "www.sample.com/gifticon"
        val request = GoalGifticonCreateRequest(goalId = 1L, gifticonURL = gifticonUrl)

        // when // then
        mockMvc
            .perform(
                post("/v1/goal-gifticon")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
    }

    @DisplayName("다짐 내 GoalGifticon을 조회한다.")
    @Test
    fun retrieve() {
        // given
        val goalId = 1L

        // when // then
        mockMvc
            .perform(
                get("/v1/goal-gifticon/$goalId")
            )
            .andDo(::print)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
    }

    @DisplayName("다짐 내 기프티콘을 수정한다.")
    @Test
    fun update() {
        // given
        val gifticonUrl = "www.sample.com/updated-gifticon"
        val request = GoalGifticonRequest(goalId = 1L, gifticonURL = gifticonUrl)

        // when // then
        mockMvc
            .perform(
                post("/v1/goal-gifticon")
                    .withCsrf()
                    .writeRequestAsContent(request)
                    .contentTypeAsJson()
            )
            .andDo(::print)
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.errorResponse").value(nullValue()))
    }
}