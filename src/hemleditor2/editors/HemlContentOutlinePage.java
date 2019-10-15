package hemleditor2.editors;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class HemlContentOutlinePage extends ContentOutlinePage {
	
	private HemlElement fInput;
	private boolean fSelectionChanging = false;
	
	public void setInput(HemlElement input) {
		this.fInput = input;

		TreeViewer viewer = getTreeViewer();
		if (viewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.setInput(fInput);					
				}
			});
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		HemlTreeContentProvider contentProvider = new HemlTreeContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(contentProvider);
		viewer.addSelectionChangedListener(this);
		viewer.setInput(fInput);
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (fSelectionChanging) return;
		fSelectionChanging = true;
		super.selectionChanged(event);
		
		if (this.fInput != null && event.getSelection() instanceof TextSelection) {
			long offset = ((TextSelection) event.getSelection()).getOffset();
			HemlElement selectElement = this.fInput.getChild(offset);
			if (selectElement != null) {
				getTreeViewer().setSelection(new StructuredSelection(selectElement), true);
			}
		}
		fSelectionChanging = false;
	}
}