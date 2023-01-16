package utils

import com.intellij.util.io.URLUtil
import dto.ProjectInfo

object UrlUtils {

    fun getDownloadUrl(projectInfo: ProjectInfo): String {
         return "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=${projectInfo.selectedSpringBootVersion}" +
                "&baseDir=${projectInfo.projectName}&groupId=${projectInfo.group}&artifactId=${projectInfo.artifact}&name=${projectInfo.projectName}" +
                "&description=${URLUtil.encodeURIComponent(projectInfo.projectDescription)}&packageName=${projectInfo.packageName}&packaging=${projectInfo.selectedPackaging}" +
                "&javaVersion=${projectInfo.selectedJavaVersion}" +
                "&dependencies=${projectInfo.selectedDependencies.map { it.id }.joinToString(separator=",")}"
    }
}

