plugins {
    id("buildlogic.java-application-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

application {
    // Define the main class for the application.
    mainClass = "com.github.nepoleg.udpgateway.server.UdpGatewayServer"
}
