package org.jetbrains.kotlin.test.helper.actions

import com.intellij.ide.ui.laf.darcula.DarculaUIUtil
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import org.jetbrains.kotlin.test.helper.state.PreviewEditorState
import org.jetbrains.kotlin.test.helper.ui.TestDataEditor
import java.awt.Component
import javax.swing.*

class ChooseAdditionalFileAction(
    private val testDataEditor: TestDataEditor,
    private val previewEditorState: PreviewEditorState
) : ComboBoxAction() {
    companion object {
        private const val NO_NAME_PROVIDED = "## no name provided ##"
    }

    private lateinit var model: DefaultComboBoxModel<FileEditor>
    private lateinit var box: ComboBox<FileEditor>

    override fun createPopupActionGroup(button: JComponent): DefaultActionGroup {
        return DefaultActionGroup()
    }

    private fun ComboBox<*>.updateBoxWidth() {
        val fileNameWithMaxLength = previewEditorState.previewEditors
            .mapNotNull { it.file?.name }
            .maxByOrNull { it.length }
            ?: NO_NAME_PROVIDED
        setMinimumAndPreferredWidth(getFontMetrics(font).stringWidth(fileNameWithMaxLength) + 80)
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        model = DefaultComboBoxModel(previewEditorState.previewEditors.toTypedArray())
        box = ComboBox(model).apply {
            item = previewEditorState.currentPreview
            updateBoxWidth()
            addActionListener {
                if (item != null) {
                    previewEditorState.chooseNewEditor(item)
                    testDataEditor.updatePreviewEditor()
                }
            }
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): Component {
                    val originalComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    text = (value as? FileEditor).presentableName
                    return originalComponent
                }
            }
            putClientProperty(DarculaUIUtil.COMPACT_PROPERTY, true)
        }

        val label = JBLabel("Available files: ")

        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(label)
            add(box)
        }
    }

    private val FileEditor?.presentableName: String
        get() = this?.file?.name ?: NO_NAME_PROVIDED

    fun updateBoxList() {
        model.removeAllElements()
        model.addAll(previewEditorState.previewEditors)
        box.item = previewEditorState.currentPreview
        box.updateBoxWidth()
    }
}
