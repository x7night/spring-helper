package ui

import MyModuleBuilder
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.impl.PsiNameHelperImpl
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.buttonGroup
import com.intellij.ui.layout.panel
import dto.DependencyGroup
import dto.ProjectInfo
import java.util.*
import java.util.regex.Pattern
import javax.swing.DefaultComboBoxModel
import javax.swing.event.DocumentEvent

class FirstStepPanel(private val context: WizardContext, val moduleBuilder: MyModuleBuilder) {
    val component: DialogPanel by lazy {
        getPanel()
    }

    private fun getPanel(): DialogPanel = panel {
        var nameField: JBTextField? = null
        var groupField: JBTextField? = null
        var artifactField: JBTextField? = null
        var packageNameField: JBTextField? = null
        titledRow("Spring Boot Project Setting") {
            row("Name:") {
                textField(moduleBuilder.projectInfo::projectName).withValidationOnInput {
                    validateName(it)
                }.withValidationOnApply {
                    validateName(it)
                }.applyToComponent {
                    nameField = this
                }
            }
            row("Group:") {
                textField(moduleBuilder.projectInfo::group).withValidationOnInput {
                    validateGroup(it)
                }.withValidationOnApply {
                    validateGroup(it)
                }.applyToComponent {
                    groupField = this
                    this.document.addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            packageNameField?.putClientProperty("groupField", groupField?.text)
                        }
                    })
                }
            }
            row("Artifact:") {
                textField(moduleBuilder.projectInfo::artifact).withValidationOnInput {
                    validateArtifact(it)
                }.withValidationOnApply {
                    validateArtifact(it)
                }.applyToComponent {
                    artifactField = this
                    this.document.addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            packageNameField?.putClientProperty("artifactField", artifactField?.text)
                        }
                    })
                }
            }
            row("Version:") { textField(moduleBuilder.projectInfo::version) }
            row("Package Name:") {
                textField(moduleBuilder.projectInfo::packageName).withValidationOnInput {
                    validatePackage(it)
                }.withValidationOnApply {
                    validatePackage(it)
                }.applyToComponent {
                    packageNameField = this
                    this.addPropertyChangeListener("groupField") {
                        this.text = this.getClientProperty("groupField") as String + "." + artifactField?.text
                    }
                    this.addPropertyChangeListener("artifactField") {
                        this.text = groupField?.text + "." + this.getClientProperty("artifactField") as String
                    }
                }
            }
            row("Java Version:") {
                comboBox(
                    DefaultComboBoxModel(moduleBuilder.projectInfo.javaVersion.toTypedArray()),
                    moduleBuilder.projectInfo::selectedJavaVersion,
                    null
                ).applyToComponent {
                    this.selectedItem = moduleBuilder.projectInfo.selectedJavaVersion
                }
            }
            row("Package:") {
                buttonGroup(moduleBuilder.projectInfo::selectedPackaging) {
                    radioButton("Jar", "jar", null).focused()
                    radioButton("War", "war", null)
                }
                label("")
            }
        }
    }.apply {
        registerValidators(context.disposable)
    }

    private fun ValidationInfoBuilder.validateName(it: JBTextField): ValidationInfo? {
        val text = it.text
        return when {
            StringUtil.isEmpty(text) -> error("Filed must be set")
            !Pattern.compile("[a-zA-Z0-9-._ ]*", Pattern.CASE_INSENSITIVE)
                .matcher(text)
                .matches() -> error("Only latin characters, digits, spaces, '-', '_' and '.' are allowed here")
            else -> null
        }
    }

    private fun ValidationInfoBuilder.validateGroup(it: JBTextField): ValidationInfo? {
        val text = it.text
        return when {
            text?.contains(" ") == true -> error("Whitespaces are not allowed here")
            text?.contains("..") == true -> error("Must not contain '..' sequences")
            StringUtil.isEmpty(text) -> error("Filed must be set")
            //
            !Pattern.compile("[a-zA-Z\\d_.]*").matcher(text)
                .matches() -> error("Only latin characters, digits, '_' and '.' are allowed here")
            text?.startsWith(".") == true || text?.endsWith(".") == true -> error("Must not start or end with '.'")
            text?.split("\\.")?.any {
                !Pattern.compile("[a-zA-Z_].*").matcher(text).matches()
            } == true -> error("A word must start with latin character or ''_'' after splitting this group by ''.''")
            //
            Pattern.compile("(^|[ .])(con|prn|aux|nul|com\\d|lpt\\d)($|[ .])", Pattern.CASE_INSENSITIVE)
                .matcher(text)
                .find() -> error("Parts 'con', 'prn', 'aux', 'nul', 'com0', ..., 'com9' and 'lpt0', ..., 'lpt9' are not allowed here")
            else -> null
        }

    }

    private fun ValidationInfoBuilder.validateArtifact(it: JBTextField): ValidationInfo? {
        val text = it.text
        return when {
            text?.contains(" ") == true -> error("Whitespaces are not allowed here")
            StringUtil.isEmpty(text) -> error("Filed must be set")
            //
            !Pattern.compile("[a-zA-Z0-9-_]*").matcher(text)
                .matches() -> error("Only latin characters, digits, '_' and '.' are allowed here")
            !Pattern.compile("[a-zA-Z_].*").matcher(text)
                .matches() -> error("Must start with latin character or '_'")
            //
            Pattern.compile("(^|[ .])(con|prn|aux|nul|com\\d|lpt\\d)($|[ .])", Pattern.CASE_INSENSITIVE)
                .matcher(text)
                .find() -> error("Parts 'con', 'prn', 'aux', 'nul', 'com0', ..., 'com9' and 'lpt0', ..., 'lpt9' are not allowed here")
            else -> null
        }
    }

    private fun ValidationInfoBuilder.validatePackage(it: JBTextField): ValidationInfo? {
        val text = it.text
        return when {
            text?.contains(" ") == true -> error("Whitespaces are not allowed here")
            StringUtil.isEmpty(text) -> error("Filed must be set")
            //
            !PsiNameHelperImpl.getInstance().isQualifiedName(text) -> error("$text is not a valid package name")
            //
            Pattern.compile("(^|[ .])(con|prn|aux|nul|com\\d|lpt\\d)($|[ .])", Pattern.CASE_INSENSITIVE)
                .matcher(text)
                .find() -> error("Parts 'con', 'prn', 'aux', 'nul', 'com0', ..., 'com9' and 'lpt0', ..., 'lpt9' are not allowed here")
            else -> null
        }
    }
}