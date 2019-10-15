package hemleditor2.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

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
					currentOffset = offsetClose + 1;
				}
			}			
		} while(offsetOpen != -1 && offsetClose != -1 && offsetOpen < offsetClose && currentOffset < fText.length());
		if (offsetClose != -1) {
			fText = fText.substring(0, offsetClose + 1);
		}
		fChildren = children.stream().toArray(HemlElement[]::new);
	}
	
	/**
	 * Create a {@link HemlElement} from the given document.
	 * @param document the document
	 * @return the created {@link HemlElement} or null if no element.
	 */
	public static HemlElement create(IDocument document) {
		return create(document.get(), null, 0);
	}
	/**
	 * Create a {@link HemlElement} from the root of the texte.
	 * @param texte the texte
	 * @return the created {@link HemlElement} or null if no element.
	 */
	public static HemlElement create(String texte) {
		return create(texte, null, 0);
	}
	/**
	 * Create a {@link HemlElement} from given offset in document.
	 * @param texte the text to find the next element
	 * @param parent the parent
	 * @param offset the offset in the document.
	 * @return the created {@link HemlElement} or null if no element in given texte.
	 */
	public static HemlElement create(String texte, HemlElement parent, long offset) {
		return texte.startsWith("{") && texte.indexOf(' ') > 2 ? new HemlElement(texte, parent, offset) : null;
	}

	/**
	 * Get the child at the given offset in the document
	 * @param offset the offset in the document
	 * @return the child or null if no child present at this offset.
	 */
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
	
	/**
	 * The qualifier of this heml element
	 * @return the qualifier.
	 */
	public String getQualifier() { return fQualifier; }
	/**
	 * The label to show in outline.
	 * @return the label
	 */
	public String getLabel() { 
		String ret = fQualifier;
		if (fTitle != null) ret += " (" + fTitle + ")";
		return ret;
	}
	/**
	 * The text of this element
	 * @return the text.
	 */
	public String getText() { return fText; }
	/**
	 * the {@link HemlElement} parent
	 * @return the parent
	 */
	public HemlElement getParent() { return fParent; }
	/**
	 * The list of children of this element
	 * @return the children
	 */
	public HemlElement[] getChildren() { return fChildren; }
	/**
	 * the offset of this element from the start of the document
	 * @return the offset.
	 */
	public long getOffset() { return fStartIndex; }
	/**
	 * The length of the text of this element.
	 * @return the length.
	 */
	public long getTextLength() { return fText.length(); }
	
	/**
	 * Generate the list of position to permit the folding of code.
	 * @param positionDest the destination list of positions
	 */
	public void generatePosition(List<Position> positionDest) {
		if (positionDest != null) {
			if (getText().contains("\n")) {
				positionDest.add(new Position((int)getOffset(), (int)getTextLength()));
				for (HemlElement child : fChildren) {
					child.generatePosition(positionDest);
				}				
			}
		}
	}
}
