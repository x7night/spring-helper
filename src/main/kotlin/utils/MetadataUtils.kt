package utils

import com.google.gson.Gson
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.HttpRequests
import dto.DependencyGroup
import dto.DownloadResult
import dto.Metadata
import dto.ProjectInfo
import utils.Slf4j.Companion.log
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.net.UnknownHostException

@utils.Slf4j
object MetadataUtils {
    fun download(file: File, progressIndicator: ProgressIndicator, projectInfo: ProjectInfo): DownloadResult = run {
        HttpRequests.request(UrlUtils.getDownloadUrl(projectInfo))
            .accept("application/vnd.initializr.v2.2+json").connect {
                val connection: URLConnection = try {
                    it.connection
                } catch (e: IOException) {
                    log.warn(
                        "Can't download project. Message (with headers info): " + HttpRequests.createErrorMessage(
                            e,
                            it,
                            true
                        )
                    )
                    throw IOException(HttpRequests.createErrorMessage(e, it, false), e)
                } catch (he: UnknownHostException) {
                    log.warn("Can't download project: " + he.message)
                    throw IOException(HttpRequests.createErrorMessage(he, it, false), he)
                }

                val contentType = connection.contentType
                val contentDisposition = connection.getHeaderField("Content-Disposition")
                val filename = getFilename(contentDisposition)
                val isZip = StringUtil.isNotEmpty(contentType) && contentType.startsWith("application/zip")
                        || filename.endsWith(".zip")
                it.saveToFile(file, progressIndicator)

                DownloadResult(isZip, file, filename)
            }
    }

    private fun getFilename(contentDisposition: String?): String {
        val filenameField = "filename="
        if (StringUtil.isEmpty(contentDisposition)) return "unknown"

        val startIdx = contentDisposition!!.indexOf(filenameField)
        val endIdx = contentDisposition.indexOf(';', startIdx)
        var fileName = contentDisposition.substring(
            startIdx + filenameField.length,
            if (endIdx > 0) endIdx else contentDisposition.length
        )
        if (StringUtil.startsWithChar(fileName, '\"') && StringUtil.endsWithChar(fileName, '\"')) {
            fileName = fileName.substring(1, fileName.length - 1)
        }
        return fileName
    }

    fun parseMetadata(projectInfo: ProjectInfo) {
        projectInfo.selectedSpringBootVersion =
            projectInfo.metadata?.bootVersion?.default.toString()
        projectInfo.selectedJavaVersion =
            projectInfo.metadata?.javaVersion?.default.toString()
        projectInfo.springBootVersion += projectInfo.metadata?.bootVersion?.values?.map { it.id } as Collection<String>
        projectInfo.dependencyGroups += projectInfo.metadata?.dependencies?.values as Collection<DependencyGroup>
    }

    fun getMetadata(): Metadata = try {
        val body = HttpRequests.request("https://start.spring.io/metadata/client")
            .accept("application/vnd.initializr.v2.2+json").connectTimeout(10000)
            .isReadResponseOnError(true)
            .readString()
        Gson().fromJson(body, Metadata::class.java)
    } catch (e: Exception) {
        log.warn("Can't get the metedata from spring.io")
        throw e
    }
}