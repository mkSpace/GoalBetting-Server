package com.whatever.raisedragon.external.aws.s3

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.whatever.raisedragon.external.config.S3Config
import org.springframework.context.annotation.Profile
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*

@Component
@Profile("!test")
class S3AgentImpl(
    private val amazonS3: AmazonS3,
    private val s3Properties: S3Config.S3Properties
) : S3Agent {
    override fun upload(
        fileInputStreamSource: InputStreamSource,
        directoryName: String,
        originalFileName: String,
        contentType: String,
        size: Long
    ): String {
        val fileName = String.format("%s%s-%s", directoryName, UUID.randomUUID(), originalFileName)
        val bucket = s3Properties.bucketName
        runCatching {
            val objectMetadata = makeObjectMetadataFromFile(contentType, size)
            amazonS3.putObject(bucket, fileName, fileInputStreamSource.inputStream, objectMetadata)
        }.onFailure { exception ->
            when (exception) {
                is IOException -> throw IllegalStateException("S3 File I/O Error", exception)
                is AmazonServiceException -> throw IllegalStateException(
                    "Failed to upload the file ($fileName)",
                    exception
                )
            }
        }
        return "${s3Properties.domain}/$fileName"
    }

    private fun makeObjectMetadataFromFile(contentType: String, size: Long): ObjectMetadata {
        return ObjectMetadata().apply {
            this.contentType = contentType
            this.contentLength = size
        }
    }
}
