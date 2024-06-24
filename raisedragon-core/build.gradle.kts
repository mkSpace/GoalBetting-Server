apply(plugin = "kotlin-jpa")

dependencies {
    implementation(project(":raisedragon-common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.0.4")
    runtimeOnly("com.mysql:mysql-connector-j:8.2.0")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api:2.1.1")
    kapt("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // jasypt
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")

    testRuntimeOnly("com.h2database:h2")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
