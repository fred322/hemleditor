package hemleditor2.editors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
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
			IDocument document = this.getDocumentProvider().getDocument(input);
			fOutlinePage.setInput(HemlElement.create(document.get()));
			document.addDocumentListener(new IDocumentListener() {
				@Override
				public void documentChanged(DocumentEvent arg0) {
					fOutlinePage.setInput(HemlElement.create(document.get()));
				}
				
				@Override
				public void documentAboutToBeChanged(DocumentEvent arg0) {					
				}
			});
			fOutlinePage.addSelectionChangedListener(this);
			this.getSelectionProvider().addSelectionChangedListener(fOutlinePage);
			
			return (T) fOutlinePage;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent arg0) {
		if (arg0.getSelection() instanceof TreeSelection) {
			Object element = ((TreeSelection) arg0.getSelection()).getFirstElement();
			if (element instanceof HemlElement) {
				HemlElement hemlElement = (HemlElement) element;
				this.selectAndReveal((int)hemlElement.getOffset(), 1);
			}
		}
	}
	
}
