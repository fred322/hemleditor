package hemleditor2.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

public class HemlTreeContentProvider implements ITreeContentProvider, ILabelProvider {
	
	@Override
	public Object[] getChildren(Object arg0) {
		return ((HemlElement)arg0).getChildren();
	}

	@Override
	public Object[] getElements(Object arg0) {
		return ((HemlElement)arg0).getChildren();
	}

	@Override
	public Object getParent(Object arg0) {
		return ((HemlElement)arg0).getParent();
	}

	@Override
	public boolean hasChildren(Object arg0) {
		HemlElement[] children = ((HemlElement)arg0).getChildren();
		return children != null && children.length > 0;
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {
		
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {		
	}

	@Override
	public void dispose() {		
	}

	@Override
	public Image getImage(Object arg0) {
		return null;
	}

	@Override
	public String getText(Object arg0) {
		return ((HemlElement)arg0).getLabel();
	}

}
