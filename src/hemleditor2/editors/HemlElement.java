package hemleditor2.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HemlElement {
	private HemlElement fParent;
	private String fText;
	private String fQualifier;
	private String fTitle;
	private HemlElement[] fChildren;

	private HemlElement(String texte, HemlElement parent) {
		fText = texte.trim();
		fParent = parent;

		fQualifier = fText.substring(1, fText.indexOf(' '));
		int currentOffset = 1;
		List<HemlElement> children = new ArrayList<>();
		int offsetClose = -1;
		int offsetOpen = -1;
		String beforeFirstChild = fText;
		offsetOpen = fText.indexOf('{', currentOffset);
		if (offsetOpen != -1) beforeFirstChild.substring(0, offsetOpen);
		Pattern pattern = Pattern.compile("%title=([^%\n}]+)");
		Matcher matcher = pattern.matcher(beforeFirstChild);
		if (matcher.find()) {
			fTitle = matcher.group(1).trim();
		}
		
		do {
			offsetClose = fText.indexOf('}', currentOffset);
			offsetOpen = fText.indexOf('{', currentOffset);
			if (offsetOpen != -1 && offsetOpen < offsetClose) { //there is a child
				HemlElement newChild = HemlElement.create(fText.substring(offsetOpen), this);
				if (newChild != null) {
					children.add(newChild);
					currentOffset = offsetOpen + newChild.getText().length();
				}
				else {
					currentOffset = offsetOpen + 1;
				}
			}			
		} while(offsetOpen != -1 && offsetClose != -1 && offsetOpen < offsetClose && currentOffset < fText.length());
		if (offsetClose != -1) {
			fText = fText.substring(0, offsetClose + 1);
		}
		fChildren = children.stream().toArray(HemlElement[]::new);
	}
	
	public static HemlElement create(String texte, HemlElement parent) {
		return texte.startsWith("{") && texte.indexOf(' ') > 2 ? new HemlElement(texte, parent) : null;
	}
	
	public String getQualifier() { return fQualifier; }
	public String getLabel() { 
		String ret = fQualifier;
		if (fTitle != null) ret += " (" + fTitle + ")";
		return ret;
	}
	public String getText() { return fText; }
	public HemlElement getParent() { return fParent; }
	public HemlElement[] getChildren() { return fChildren; }
}
