package step

import MyModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import dto.MyModuleContext
import groovy.util.logging.Slf4j
import ui.FirstStepPanel
import utils.MetadataUtils
import javax.swing.JComponent


@Slf4j
class FirstStep(private val context: WizardContext, private val myModuleContext: MyModuleContext) : ModuleWizardStep() {

    private var panel: DialogPanel? = null

    override fun getComponent(): JComponent =
        FirstStepPanel(context, myModuleContext.moduleBuilder).component.also { panel = it }

    override fun updateDataModel() {
        panel?.apply()
        myModuleContext.moduleBuilder.projectInfo.selectedDependencies.clear()
    }

    override fun validate(): Boolean {
        if (myModuleContext.moduleBuilder.projectInfo.metadata == null) {
            updateDataModel()
            setProjectInfoFromWeb()
        }
        if (myModuleContext.moduleBuilder.projectInfo.metadata == null) {
            return false
        }
        return super.validate()
    }

    private fun setProjectInfoFromWeb() {
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            try {
                myModuleContext.moduleBuilder.projectInfo.metadata = MetadataUtils.getMetadata()
                MetadataUtils.parseMetadata(myModuleContext.moduleBuilder.projectInfo)
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeAndWait {
                    Messages.showErrorDialog(
                        component,
                        "Can't get the metadata from the \"https://spring.io\", caused by: ${e.message}, please check the internet connection.",
                        "Error"
                    )
                }
            }
        }, "Getting the metadata", true, null)
    }
}