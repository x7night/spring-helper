import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.io.ZipUtil
import dto.DownloadResult
import dto.MyModuleContext
import dto.ProjectInfo
import groovy.util.logging.Slf4j
import org.jetbrains.idea.maven.project.MavenProjectsManager
import step.FirstStep
import step.SecondStep
import step.ThirdStep
import utils.MetadataUtils
import utils.Slf4j.Companion.log
import java.io.File
import java.nio.file.Path

@utils.Slf4j
class MyModuleBuilder : JavaModuleBuilder() {
    val projectInfo = ProjectInfo()
//    private val log = logger<MyModuleBuilder>()
    init {
        ApplicationManager.getApplication().executeOnPooledThread{
            try {
            projectInfo.metadata = MetadataUtils.getMetadata()
            MetadataUtils.parseMetadata(projectInfo)
            }catch (e: Exception){
                log.error("Get metadata in $presentableName initialization failed, caused by:${e.message}", e)
            }
        }
    }

    override fun getBuilderId(): String {
        return "spring-helper"
    }

    override fun getGroupName(): String {
        return "spring-tool"
    }

    override fun getPresentableName(): String {
        return "spring helper"
    }

    override fun getModuleTypeName(): String {
        return "springHelper"
    }

    override fun getCustomOptionsStep(context: WizardContext?, parentDisposable: Disposable?): FirstStep? {
        return context?.let { FirstStep(it, MyModuleContext(this, this.projectInfo, parentDisposable!!)) }
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return arrayOf(SecondStep(wizardContext, MyModuleContext(this, this.projectInfo, wizardContext.disposable)))
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep {
        ApplicationManager.getApplication().defaultModalityState
        return ThirdStep(settingsStep, this)
    }

    override fun setupRootModel(rootModel: ModifiableRootModel) {
        // 修改路径
        this.sourcePaths = listOf(Pair.create("${this.contentEntryPath}${File.separator}src${File.separator}main", ""))
        super.setupRootModel(rootModel)
        // 解压
        extract()
        // 设置为maven工程
        ApplicationManager.getApplication().invokeLater {
            val mavenProjectsManager = MavenProjectsManager.getInstance(rootModel.project)
            mavenProjectsManager.addManagedFiles(
                listOf(
                    VfsUtil.findFile(
                        Path.of("${this.contentEntryPath}${File.separator}pom.xml"),
                        true
                    )
                )
            )
        }
    }

    private fun extract() {
        val downloadFile: DownloadResult = projectInfo.downloadResult!!
        val tempFile = downloadFile.tempFile
        val path: String = contentEntryPath!!
        val contentDir = File(path).parentFile

        if (downloadFile.isZip) {
            ZipUtil.extract(tempFile.toPath(), contentDir.toPath(), null)
            fixExecutableFlag(contentDir, "gradlew")
            fixExecutableFlag(contentDir, "mvnw")
        } else {
            FileUtil.copy(tempFile, File(contentDir, downloadFile.filename))
        }

        val vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(contentDir)
        VfsUtil.markDirtyAndRefresh(false, true, false, vf)
    }

    private fun fixExecutableFlag(containingDir: File, relativePath: String) {
        val toFix = File(containingDir, relativePath)
        if (toFix.exists()) {
            toFix.setExecutable(true, false)
        }
    }
}