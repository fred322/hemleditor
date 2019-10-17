package hemleditor2.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;

public class HemlElement {
	private static final Pattern FirstLinePattern = Pattern.compile("^\\{?[\\s\\S]*?\\}");
	private static final Pattern QualifierPattern = Pattern.compile("^\\s*\\{(\\S+)([^\\n]*%title=([^%}\\n]+))?[^\\n]*$", Pattern.MULTILINE);
	private static final Pattern EndBlockPattern = Pattern.compile("^[^#\\n]*(\\})[ \t]*(?:#[^\\n]*)?$", Pattern.MULTILINE);
	private static final Pattern PlantUmlBlockPattern = Pattern.compile("^(\\{#[^#]+#\\})");
	private static final Pattern CodeBlockPattern = Pattern.compile("^(\\{![^!]+!\\})");
	private HemlElement fParent;
	private String fText;
	private String fQualifier;
	private String fTitle;
	private long fStartIndex;
	private HemlElement[] fChildren;

	private HemlElement(String texte, HemlElement parent, long offset) {
		fParent = parent;
		update(texte, offset);
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
		return new HemlElement(texte, parent, offset);
	}

	/**
	 * Update this element and all its children
	 * @param texte the new texte.
	 * @return true if ok.
	 */
	public boolean update(String texte) {
		return update(texte, 0);
	}
	/**
	 * Update this element and all its children
	 * @param texte the new texte.
	 * @param offset the new offset in the document.
	 * @return true if ok.
	 */
	public boolean update(String texte, long offset) {
		fText = texte.trim();
		fStartIndex = offset;
		
		if (fStartIndex == 0) {
			Matcher firstLineMatcher = FirstLinePattern.matcher(fText);
			if (firstLineMatcher.find()) {
				fStartIndex = firstLineMatcher.end() + 1;
				fText = fText.substring((int) fStartIndex);
			}
		}
		return update();
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
	 * The list of children of this element
	 * @return the children
	 */
	public HemlElement[] getChildren(Predicate<HemlElement> filter) { return Arrays.asList(fChildren).stream().filter(filter).toArray(HemlElement[]::new); }
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
	
	private boolean update() {
		Matcher matcherQualifier = QualifierPattern.matcher(fText);
		Matcher endBlockMatcher = EndBlockPattern.matcher(fText);
		if (matcherQualifier.find()) {
			fQualifier = matcherQualifier.group(1);
			if (matcherQualifier.groupCount() > 2) {
				fTitle = matcherQualifier.group(3);
			}
		}
		int currentOffset = matcherQualifier.start(1) + 1;
		int currentOffsetForClose = 0;
		int offsetClose = -1;
		int offsetOpen = -1;
		
		List<HemlElement> children = fChildren != null ? new ArrayList<>(Arrays.asList(fChildren)) : new ArrayList<>();
		int currentIdx = 0;
		Matcher matcherPlantUml = PlantUmlBlockPattern.matcher(fText);
		Matcher matcherCode = CodeBlockPattern.matcher(fText);
		if (matcherPlantUml.find() || matcherPlantUml.matches()) {
			fText = matcherPlantUml.group();
		}
		else if (matcherCode.find()) {
			fText = matcherCode.group();			
		}
		else {			
			do {
				offsetClose = endBlockMatcher.find(currentOffsetForClose) ? endBlockMatcher.start(1) : -1;
				// search next qualifier.
				if (matcherQualifier.find(currentOffset) && (offsetOpen = matcherQualifier.start(1) - 1) < offsetClose) { //there is a child
					HemlElement newChild = null;
					long subOffset = fStartIndex + offsetOpen;
					String subText = fText.substring(offsetOpen);
					if (currentIdx < children.size()) {
						newChild = children.get(currentIdx);
						newChild.update(subText, subOffset);
					}
					else {
						newChild = new HemlElement(subText, this, subOffset);
						children.add(newChild);
					}
					currentIdx++;
					currentOffset = offsetOpen + newChild.getText().length();
					currentOffsetForClose = currentOffset;
				} else break;
			} while(offsetClose != -1 && currentOffset < fText.length());			
		}
		if (offsetClose != -1) {
			fText = fText.substring(0, offsetClose + 1);
		}
		if (currentIdx < children.size()) children = children.subList(0, currentIdx);
		fChildren = children.stream().toArray(HemlElement[]::new);
		return true;
	}
	
}
