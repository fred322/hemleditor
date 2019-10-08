package hemleditor2.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class HemlEditor extends TextEditor implements ISelectionChangedListener {

	private HemlContentOutlinePage fOutlinePage;
	public HemlEditor() {
		super();
		setSourceViewerConfiguration(new HemlConfiguration());
		setDocumentProvider(new HemlDocumentProvider());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (fOutlinePage == null) fOutlinePage = new HemlContentOutlinePage();
			IEditorInput input = this.getEditorInput();
			String content = this.getDocumentProvider().getDocument(input).get();
			fOutlinePage.setInput(HemlElement.create(content, null));
			fOutlinePage.addSelectionChangedListener(this);
			return (T) fOutlinePage;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent arg0) {
		System.out.println("Selection changed!");
		if (arg0.getSelection() instanceof TreeSelection) {
			Object element = ((TreeSelection) arg0.getSelection()).getFirstElement();
			if (element instanceof HemlElement) {
				System.out.println("Selected: " + ((HemlElement) element).getLabel());
			}
		}
	}
	
}
