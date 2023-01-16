plugins {
    java
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.intellij") version "1.1.2"
    idea
}
 
group = "com.github.x7night"
version = "1.0.0"

repositories {
    mavenCentral()
    maven ("http://maven.aliyun.com/nexus/content/groups/public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    implementation("io.spring.initializr:initializr-metadata:0.10.2")
    implementation("io.spring.initializr:initializr-generator:0.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

intellij {
    plugins.set(listOf("Lombook Plugin", "java", "maven", "gradle"))
//    version.set("211-EAP-SNAPSHOT")
}