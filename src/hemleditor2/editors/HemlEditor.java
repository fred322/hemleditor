package hemleditor2.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class HemlEditor extends TextEditor {

	public HemlEditor() {
		super();
		setSourceViewerConfiguration(new HemlConfiguration());
		setDocumentProvider(new HemlDocumentProvider());
	}
}
