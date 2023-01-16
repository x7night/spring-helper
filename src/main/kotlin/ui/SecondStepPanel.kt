package ui

import MyModuleBuilder
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ui.CheckBoxList
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.Link
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import dto.Dependency
import io.spring.initializr.generator.version.Version
import io.spring.initializr.generator.version.VersionParser
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.ScrollPaneConstants

class SecondStepPanel(val wizardContext: WizardContext, val moduleBuilder: MyModuleBuilder) {
    fun getPanel() = panel {
        val bootVersion = DefaultComboBoxModel(moduleBuilder.projectInfo.springBootVersion.toTypedArray())

        val searchTextField = SearchTextField(false)

        val description = JBTextArea("").apply {
            this.isEditable = false
            this.lineWrap = true
            this.wrapStyleWord = true
        }

        val link = Link("Reference") {
            moduleBuilder.projectInfo.curDependency?.link?.reference?.href.let {
                if (it != null) {
                    BrowserUtil.browse(it)
                }
            }
        }

        val selectedDependencies = CheckBoxList<Dependency>().apply {
            this.clear()
            this.setEmptyText("Select some dependencies for project")
            moduleBuilder.projectInfo.selectedDependencies.map { this.addItem(it, it.name, true) }
        }

        val dependenciesList = CheckBoxList<Dependency>().apply {
            this.clear()
            moduleBuilder.projectInfo.curGroup?.values?.filter {
                it.versionRange == null || VersionParser.DEFAULT.parseRange(it.versionRange)
                    .match(Version.safeParse(bootVersion.selectedItem as String))
            }?.forEach {
                this.addItem(it, it.name, moduleBuilder.projectInfo.selectedDependencies.contains(it))
            }

            this.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (moduleBuilder.projectInfo.curDependency != this@apply.getItemAt(this@apply.locationToIndex(e.point))) {
                        moduleBuilder.projectInfo.curDependency =
                            this@apply.getItemAt(this@apply.locationToIndex(e.point))
                        description.text = moduleBuilder.projectInfo.curDependency?.description
                        link.isVisible = moduleBuilder.projectInfo.curDependency?.link != null
                    }
                    this@apply.updateUI()
                }
            })
        }

        val groupList = JBList(moduleBuilder.projectInfo.dependencyGroups.map { it.name }).apply {
            this.setSelectedValue(moduleBuilder.projectInfo.curGroup?.name, true)
            this.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (moduleBuilder.projectInfo.curGroup != moduleBuilder.projectInfo.dependencyGroups[this@apply.selectedIndex]) {
                        moduleBuilder.projectInfo.curGroup =
                            moduleBuilder.projectInfo.dependencyGroups[this@apply.selectedIndex]
                        clearAndUpdateDependencyList(dependenciesList, bootVersion)
                    }
                }
            })
        }

        searchTextField.textEditor.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                /** do nothing */
            }

            override fun keyPressed(e: KeyEvent?) {
                /** do nothing */
            }

            override fun keyReleased(e: KeyEvent?) {
                val searchKey = searchTextField.text.lowercase(Locale.getDefault())
                moduleBuilder.projectInfo.dependencyGroups.firstOrNull {
                    it.values.filter { dependency ->
                        dependency.versionRange == null || VersionParser.DEFAULT.parseRange(dependency.versionRange)
                            .match(Version.safeParse(bootVersion.selectedItem as String))
                    }.filter { dependency ->
                        "${it.name}:${dependency.name}".lowercase(Locale.getDefault()).replace(" ", "")
                            .contains(searchKey)
                    }.any()
                }?.let {
                    moduleBuilder.projectInfo.curGroup = it
                    moduleBuilder.projectInfo.curDependency = it.values.first { ele ->
                        ele.name.lowercase(Locale.getDefault()).replace(" ", "")
                            .contains(searchKey)
                    }
                    groupList.setSelectedValue(it.name, true)
                    groupList.updateUI()
                    clearAndUpdateDependencyList(dependenciesList, bootVersion)
                }
            }
        })

        dependenciesList.setCheckBoxListListener { index, selected ->
            if (selected) {
                moduleBuilder.projectInfo.selectedDependencies.add(dependenciesList.getItemAt(index)!!)
                selectedDependencies.addItem(
                    dependenciesList.getItemAt(index)!!,
                    dependenciesList.getItemAt(index)?.name,
                    true
                )
            } else {
                moduleBuilder.projectInfo.selectedDependencies.remove(dependenciesList.getItemAt(index)!!)
                selectedDependencies.clear()
                moduleBuilder.projectInfo.selectedDependencies.map { selectedDependencies.addItem(it, it.name, true) }
            }
            selectedDependencies.updateUI()
        }

        selectedDependencies.setCheckBoxListListener { index, selected ->
            if (!selected) {
                val curItem = selectedDependencies.getItemAt(index)
                selectedDependencies.clear()
                moduleBuilder.projectInfo.selectedDependencies.remove(curItem)
                moduleBuilder.projectInfo.selectedDependencies.map { selectedDependencies.addItem(it, it.name, true) }
                dependenciesList.setItemSelected(curItem, selected)
                dependenciesList.updateUI()
            }
        }

        // UI
        titledRow("") {
            cell {
                label("Filter:")
                searchTextField()
            }
            cell { placeholder() }
            cell {
                label("SpringBoot:").withLeftGap()
                comboBox(bootVersion, moduleBuilder.projectInfo::selectedSpringBootVersion, null).applyToComponent {
                    this.selectedItem = moduleBuilder.projectInfo.metadata?.bootVersion?.default
                    this.addItemListener {
                        if (it.stateChange == 1) {
                            clearSelectedDependencies(selectedDependencies)
                            clearAndUpdateDependencyList(dependenciesList, bootVersion)
                        }
                    }
                }
                ContextHelpLabel("","Note: Dependency will be filtered by SpringBoot version. Change the version will clear all selected dependencies")()
                    .applyToComponent { this.icon =  AllIcons.General.ContextHelp}
            }
        }
        row {
            label("Dependency Group")
            label("Dependency")
            label("Selected Dependencies")
        }
        row {
            scrollPane(groupList).growPolicy(GrowPolicy.SHORT_TEXT).constraints(pushY)
            scrollPane(dependenciesList).growPolicy(GrowPolicy.SHORT_TEXT).constraints(pushY)
            scrollPane(selectedDependencies).growPolicy(GrowPolicy.SHORT_TEXT).constraints(pushY)
        }
        blockRow {  }
        row {
            cell {
                label("Dependency Description")
                link().visible(moduleBuilder.projectInfo.curDependency?.link != null)
            }
        }
        row {
            scrollPane(description).constraints(pushX)
                .applyToComponent {
                    this.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                }
        }
    }

    private fun clearAndUpdateDependencyList(
        dependenciesList: CheckBoxList<Dependency>,
        bootVersion: DefaultComboBoxModel<String>
    ) {
        dependenciesList.clear()
        moduleBuilder.projectInfo.curGroup?.values?.filter {
            it.versionRange == null || VersionParser.DEFAULT.parseRange(it.versionRange)
                .match(Version.safeParse(bootVersion.selectedItem as String))
        }?.forEach {
            dependenciesList.addItem(it, it.name, moduleBuilder.projectInfo.selectedDependencies.contains(it))
        }
    }

    private fun clearSelectedDependencies(selectedDependencies: CheckBoxList<*>) {
        selectedDependencies.clear()
        selectedDependencies.updateUI()
        moduleBuilder.projectInfo.selectedDependencies.clear()
    }
}