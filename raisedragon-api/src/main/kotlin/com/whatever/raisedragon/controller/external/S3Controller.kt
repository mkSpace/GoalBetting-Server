package com.whatever.raisedragon.controller.external

import com.whatever.raisedragon.applicationservice.S3ApplicationService
import com.whatever.raisedragon.common.Response
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "S3", description = "S3 API")
@RestController
@RequestMapping("/v1/s3")
class S3Controller(
    private val s3ApplicationService: S3ApplicationService
) {

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun create(
        @RequestPart multipartFile: MultipartFile
    ): Response<String> {
        return Response.success(
            s3ApplicationService.upload(multipartFile)
        )
    }
}