package hemleditor2.editors;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;

public class HemlTreeContentProvider implements ITreeContentProvider, ILabelProvider {
	private static Pattern CHILD_TO_HIDE = Pattern.compile("^([!#]|em|kw])");
	
	@Override
	public Object[] getChildren(Object arg0) {
		return ((HemlElement)arg0).getChildren(child -> !CHILD_TO_HIDE.matcher(child.getQualifier()).matches());
	}

	@Override
	public Object[] getElements(Object arg0) {
		return getChildren(arg0);
	}

	@Override
	public Object getParent(Object arg0) {
		return ((HemlElement)arg0).getParent();
	}

	@Override
	public boolean hasChildren(Object arg0) {
		Object[] children = getChildren(arg0);
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
