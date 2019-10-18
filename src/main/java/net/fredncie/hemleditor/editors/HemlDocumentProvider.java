package net.fredncie.hemleditor.editors;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class HemlDocumentProvider extends FileDocumentProvider {

	private IDocumentListener fDocListener;
	
	public void setDocumentListener(IDocumentListener listener) {
		fDocListener = listener;
	}
	
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null)
		{
			IDocumentPartitioner partitioner = new FastPartitioner(new HemlPartitionScanner(), HemlPartitionScanner.TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);

			document.addDocumentListener(new IDocumentListener() {
				@Override
				public void documentChanged(DocumentEvent arg0) {
					if (fDocListener != null) fDocListener.documentChanged(arg0);
				}
				
				@Override
				public void documentAboutToBeChanged(DocumentEvent arg0) {
					if (fDocListener != null) fDocListener.documentAboutToBeChanged(arg0);
				}
			});
		}
		return document;
	}
}
