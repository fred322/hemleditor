/**
 * 
 */
package net.fredncie.hemleditor.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */
public class FormatCommandHandler extends AbstractHandler {

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof HemlEditor) {
            IDocument document = ((HemlEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
            
            String docValue = document.get();
            HemlElement element = HemlElement.create(docValue);
            StringBuilder output = new StringBuilder(docValue.length());
            element.write(output, new HemlIndenter());
            
            document.set(output.toString());
        }
        return null;
    }

}
