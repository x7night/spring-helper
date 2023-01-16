package step

import MyModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import utils.MetadataUtils
import javax.swing.JComponent

class ThirdStep(val settingsStep: SettingsStep, val myModuleBuilder: MyModuleBuilder) :
    ModuleWizardStep() {
    init {
        settingsStep.moduleNameLocationSettings?.moduleName = myModuleBuilder.projectInfo.projectName
    }

    override fun getComponent(): JComponent? {
        return null
    }

    override fun updateDataModel() {
        myModuleBuilder.projectInfo.baseDir = settingsStep.context.projectName
        downloadZip()
    }

    private fun downloadZip() {
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            try {
                val progressIndicator = ProgressManager.getInstance().progressIndicator
                progressIndicator.text = "Please wait ..."
                myModuleBuilder.projectInfo.downloadResult =
                    MetadataUtils.download(
                        FileUtil.createTempFile("spring-helper", ".tmp", true),
                        progressIndicator,
                        myModuleBuilder.projectInfo
                    )
            } catch (e: Exception) {
                Messages.showErrorDialog("Download failed...", "Error Information")
            }
        }, "Downloading Required Files...", true, null)
    }
}