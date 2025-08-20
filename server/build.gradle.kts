plugins {
    id("buildlogic.java-application-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(libs.agrona)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(project(":utilities"))
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

application {
    // Define the main class for the application.
    mainClass = "com.github.nepoleg.udpgateway.server.UdpGatewayServer"
}
