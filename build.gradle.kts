allprojects {
    subprojects {
        apply(plugin = "java")

//        java {
//            toolchain {
//                languageVersion.set(JavaLanguageVersion.of(17))
//            }
//        }

        tasks.withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }

    }
}