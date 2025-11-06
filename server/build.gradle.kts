plugins {
    id("buildlogic.java-application-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("application")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(libs.agrona)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.netty.all)
    implementation(project(":common"))
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
