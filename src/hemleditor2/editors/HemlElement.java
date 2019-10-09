package hemleditor2.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;

public class HemlElement {
	private static final Pattern QualifierPattern = Pattern.compile("\\{([^\\s\n]+)");
	private static final Pattern TitlePattern = Pattern.compile("%title=([^%\n}]+)");
	private HemlElement fParent;
	private String fText;
	private String fQualifier;
	private String fTitle;
	private long fStartIndex;
	private HemlElement[] fChildren;

	private HemlElement(String texte, HemlElement parent, long offset) {
		fText = texte.trim();
		fParent = parent;
		fStartIndex = offset;

		Matcher matcherQualifier = QualifierPattern.matcher(texte);
		if (matcherQualifier.find()) {
			fQualifier = matcherQualifier.group(1);			
		}
		int currentOffset = 1;
		List<HemlElement> children = new ArrayList<>();
		int offsetClose = -1;
		int offsetOpen = -1;
		String beforeFirstChild = fText;
		offsetOpen = fText.indexOf('{', currentOffset);
		if (offsetOpen != -1) beforeFirstChild = beforeFirstChild.substring(0, offsetOpen);
		Matcher matcher = TitlePattern.matcher(beforeFirstChild);
		if (matcher.find()) {
			fTitle = matcher.group(1).trim();
		}
		
		do {
			offsetClose = fText.indexOf('}', currentOffset);
			offsetOpen = fText.indexOf('{', currentOffset);
			if (offsetOpen != -1 && offsetOpen < offsetClose) { //there is a child
				HemlElement newChild = HemlElement.create(fText.substring(offsetOpen), this, offset + offsetOpen);
				if (newChild != null) {
					if (newChild.getQualifier().length() >= 4) {
						children.add(newChild);						
					}
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
	public static HemlElement create(IDocument document) {
		return create(document.get(), null, 0);
	}
	public static HemlElement create(String texte) {
		return create(texte, null, 0);
	}
	public static HemlElement create(String texte, HemlElement parent, long offset) {
		return texte.startsWith("{") && texte.indexOf(' ') > 2 ? new HemlElement(texte, parent, offset) : null;
	}
	
	public HemlElement getChild(long offset) {
		HemlElement ret = null;
		if (offset >= this.fStartIndex && offset < this.fStartIndex + this.getTextLength()) {
			for (HemlElement child : fChildren) {
				ret = child.getChild(offset);
				if (ret != null) break;
			}
			if (ret == null) ret = this;			
		}
		return ret;
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
	public long getOffset() { return fStartIndex; }
	public long getTextLength() { return fText.length(); }
}
