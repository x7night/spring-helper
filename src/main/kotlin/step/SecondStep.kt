package step

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.ui.DialogPanel
import dto.MyModuleContext
import ui.SecondStepPanel
import javax.swing.JComponent

class SecondStep(
    val wizardContext: WizardContext,
    val myModuleContext: MyModuleContext
) : ModuleWizardStep() {
    private var panel: DialogPanel? = null
    override fun getComponent(): JComponent = SecondStepPanel(wizardContext, myModuleContext.moduleBuilder).getPanel().also { panel = it }

    override fun updateDataModel() {
        panel?.apply()
    }
}