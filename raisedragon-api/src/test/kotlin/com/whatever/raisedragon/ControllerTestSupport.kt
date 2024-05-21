package com.whatever.raisedragon

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.raisedragon.applicationservice.betting.BettingApplicationService
import com.whatever.raisedragon.applicationservice.goalgifticon.GoalGifticonApplicationService
import com.whatever.raisedragon.applicationservice.goalproof.GoalProofApplicationService
import com.whatever.raisedragon.controller.betting.BettingController
import com.whatever.raisedragon.controller.goalgifticon.GoalGifticonController
import com.whatever.raisedragon.controller.goalproof.GoalProofController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

@WebMvcTest(
    controllers = [
        BettingController::class,
        GoalGifticonController::class,
        GoalProofController::class
    ]
)
@ActiveProfiles("test")
abstract class ControllerTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockBean
    protected lateinit var bettingApplicationService: BettingApplicationService

    @MockBean
    protected lateinit var goalGifticonApplicationService: GoalGifticonApplicationService

    @MockBean
    protected lateinit var goalProofApplicationService: GoalProofApplicationService

    protected fun MockHttpServletRequestBuilder.withCsrf(): MockHttpServletRequestBuilder {
        return with(SecurityMockMvcRequestPostProcessors.csrf())
    }

    protected fun MockHttpServletRequestBuilder.writeRequestAsContent(request: Any): MockHttpServletRequestBuilder {
        return content(objectMapper.writeValueAsString(request))
    }

    protected fun MockHttpServletRequestBuilder.contentTypeAsJson(): MockHttpServletRequestBuilder {
        return contentType(MediaType.APPLICATION_JSON)
    }
}