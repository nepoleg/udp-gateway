plugins {
    id("buildlogic.java-application-conventions")
}

dependencies {
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.pico.cli)
    implementation(project(":common"))
}

application {
    // Define the main class for the application.
    mainClass = "com.github.nepoleg.udpgateway.client.UdpGatewayClientApplication"
}
