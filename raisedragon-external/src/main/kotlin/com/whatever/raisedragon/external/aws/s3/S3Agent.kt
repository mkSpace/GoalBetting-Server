package com.whatever.raisedragon.external.aws.s3

import org.springframework.core.io.InputStreamSource

interface S3Agent {

    /**
     * @param fileInputStreamSource 사용자가 전송할 파일 InputStreamSource 입니다.
     * @param directoryName S3 버킷에 파일을 저장할 Directory 이름입니다.
     * @param originalFileName 사용자가 전송할 정적파일의 원본 파일 이름입니다.
     * @param contentType 사용자가 전송할 파일 contentType 입니다.
     * @param size 사용자가 전송할 파일의 크기 입니다.
     * @return 해당 파일을 S3에 업로드 한 이후, 해당 파일에 접근할 수 있는 url을 반환해합니다.
     */
    fun upload(
        fileInputStreamSource: InputStreamSource,
        directoryName: String,
        originalFileName: String,
        contentType: String,
        size: Long
    ): String
}
