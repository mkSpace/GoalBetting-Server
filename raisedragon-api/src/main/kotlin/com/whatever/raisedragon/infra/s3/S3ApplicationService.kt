package com.whatever.raisedragon.infra.s3

import com.whatever.raisedragon.external.aws.s3.S3Agent
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Profile("!test")
class S3ApplicationService(
    private val s3Agent: S3Agent
) : FileUploader {

    override fun upload(multipartFile: MultipartFile): String {
        return s3Agent.upload(
            fileInputStreamSource = multipartFile,
            directoryName = S3_PREFIX_DIRECTORY,
            originalFileName = multipartFile.originalFilename ?: DEFAULT_ORIGINAL_FILE_NAME,
            contentType = multipartFile.contentType ?: DEFAULT_CONTENT_TYPE,
            size = multipartFile.size
        )
    }

    companion object {
        private const val S3_PREFIX_DIRECTORY = "gifticon/"

        private const val DEFAULT_ORIGINAL_FILE_NAME = "default-file-name"
        private const val DEFAULT_CONTENT_TYPE = "multipart/form-data"
    }
}
