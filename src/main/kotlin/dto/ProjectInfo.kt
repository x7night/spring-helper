package dto

class ProjectInfo {
    var metadata: Metadata? = null

    var projectLanguage: String = "Java"
    var group: String = "com.example"
    var artifact: String = "demo"
    var version: String = "1.0.0-SNAPSHOT"
    var projectType: String = "maven"
    var javaVersion = mutableListOf("11", "16", "1.8")
    var projectName: String = "demo"
    var projectDescription: String = "a demo"
    var packageName: String = ""
        get() = "$group.$artifact"
    var packaging: List<String> = arrayListOf("jar", "war")
    var springBootVersion = mutableListOf("2.5.0")
    var dependencyGroups = mutableListOf<DependencyGroup>()

    var selectedJavaVersion: String = "11"
    var selectedPackaging: String = "jar"
    var selectedSpringBootVersion: String = "2.5.0"
    val selectedDependencies = mutableListOf<Dependency>()

    var curGroup:DependencyGroup? = null
    var curDependency: Dependency? = null
    var dependencyDescription: String = ""

    var downloadResult: DownloadResult? = null
    var baseDir = "demo"
}
