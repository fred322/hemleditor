package hemleditor2.editors;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class HemlContentOutlinePage extends ContentOutlinePage {
	
	private HemlElement fInput;
	
	public void setInput(HemlElement input) {
		this.fInput = input;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		HemlTreeContentProvider contentProvider = new HemlTreeContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(contentProvider);
		viewer.addSelectionChangedListener(this);;
		viewer.setInput(fInput);
	}
}