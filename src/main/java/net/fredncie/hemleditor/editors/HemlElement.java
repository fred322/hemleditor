package net.fredncie.hemleditor.editors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.Position;

public class HemlElement {
    private static final String DEFAULT_INDENT = String.format("%4s", "");
    
	private static final Pattern FirstLinePattern = Pattern.compile("^\\{\\?[^\\}]*\\}");
	private static final Pattern QualifierPattern = Pattern.compile("^[^#\\{]*\\{(\\S+)([^\\n]*%(?:title|language)=([^%}\\n]+))?[^\\n]*$", Pattern.MULTILINE);
	private static final Pattern EndBlockPattern = Pattern.compile("^[^#\\}]*(\\})[^\\n]*$", Pattern.MULTILINE);
	private static final Pattern CommentBlockEndPattern = Pattern.compile("(#\\})");
    private static final Pattern CodeBlockEndPattern = Pattern.compile("(!\\})");
    private static final Pattern attributesPattern = Pattern.compile("%([^%]+)=([^%}]+)");
	private HemlElement fParent;
	private String fText;
	private String fQualifier;
	private String fTitle;
	private long fStartIndex;
	private HemlElement[] fChildren;
	private final Map<String, String> fDocOptions = new HashMap<>();

	private HemlElement(String texte, HemlElement parent, long offset) {
		fParent = parent;
		update(texte, offset);
	}
	
	public static HemlElement create(File file) {
	    HemlElement ret = null;
	    try {
            ret = create(IOUtils.toString(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
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
			
				String firstLine = firstLineMatcher.group();
                Matcher matcherAttribute = attributesPattern.matcher(firstLine);
                while (matcherAttribute.find()) {
                    fDocOptions.put(matcherAttribute.group(1).trim(), matcherAttribute.group(2).trim());
                }
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
        if (fQualifier.startsWith("#")) ret = "comment";
        if (fQualifier.startsWith("!")) ret = "code block";
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

    public void write(StringBuilder output, int indentation) {
        write(output, indentation, 0);
    }
    
	public void write(StringBuilder output, int indentation, int previousIndentation) {
        List<HemlElement> children = fChildren != null ? new ArrayList<>(Arrays.asList(fChildren)) : new ArrayList<>();
        long currentOffset = 0;
        String firstLineIndentationStr = indentation > 0 ? String.format("%" + indentation + "s", "") : "";
        boolean firstLine = true;
        if (output.length() == 0 && !fDocOptions.isEmpty()) {
            output.append("{?set");
            for (Entry<String, String> attribute : fDocOptions.entrySet()) {
                output.append(" %").append(attribute.getKey()).append("=").append(attribute.getValue());
            }
            output.append("}\n");
        }
        for (HemlElement child : children) {
            long childOffset = child.getOffset() - getOffset();
            if (!"kw".equals(child.getQualifier()) && !"i".equals(child.getQualifier()) && !"em".equals(child.getQualifier())) {
                if (currentOffset < childOffset) {
                    String substring = fText.substring((int)currentOffset, (int)childOffset);
                    writeText(substring, output, firstLineIndentationStr, previousIndentation, firstLine, false);
                }
                firstLine = false;

                String beforeText = fText.substring(0, (int)childOffset);
                int childPreviousIndent = Math.max(0, (int)childOffset - (beforeText.lastIndexOf('\n') + 1));
                child.write(output, indentation + 4, childPreviousIndent);
                currentOffset = childOffset + child.getTextLength();                
            }
        }
        if (currentOffset < getTextLength()) {
            String substring = fText.substring((int)currentOffset);
            writeText(substring, output, firstLineIndentationStr, previousIndentation, firstLine, true);            
        }
	}
	
	private void writeText(String texte, StringBuilder output, String indentation, 
	        int previousIndent, boolean hasFirstLine, boolean hasLastLine) {
	    String subIndentation = indentation;
	    boolean keepSpaces = false;
	    if (!"#".equals(getQualifier()) && !"!".equals(getQualifier())) {
	        subIndentation += DEFAULT_INDENT;
	    }
	    else {
            // the code must be at the indentation 0
	        if ("!".equals(getQualifier())) {
	            indentation = "";
	            subIndentation = "";
	        }
	        keepSpaces = true;
	    }
	    int itemLevel = 0;
	    int previousItemSpacesCount = 0;
	    String[] lines = texte.replace("\t", DEFAULT_INDENT).split("\n");
	    for (int idxLine = 0; idxLine < lines.length; idxLine++) {
	        String line = lines[idxLine];
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                boolean firstLine = hasFirstLine && idxLine == 0;
                if (hasLastLine && idxLine == lines.length - 1 && trimmed.length() <= 2 || firstLine) {
                    output.append(indentation);
                }
                else output.append(subIndentation);
                
                if (keepSpaces && !firstLine) line = line.substring(previousIndent);
                else {
                    line = line.replaceAll("\\s*$", "");
                    String trimmedLine = line.replaceAll("^\\s*", "");
                    int leftSpacesCount = line.length() - trimmedLine.length();
                    if (trimmedLine.startsWith("-")) {
                        if (leftSpacesCount > previousItemSpacesCount) {
                            itemLevel++;
                            previousItemSpacesCount = leftSpacesCount;
                        }
                        else if (leftSpacesCount < previousItemSpacesCount) {
                            itemLevel--;
                            previousItemSpacesCount = leftSpacesCount;
                            if (itemLevel < 0) {
                                System.err.println("HEML error with items in block: " + getLabel());
                            }
                        }
                    }

                    if (itemLevel > 0) {
                        output.append(String.format("%" + (itemLevel * 4) + "s", ""));                            
                    }
                    else itemLevel = 0;
                    line = trimmedLine;
                }                
                output.append(line).append("\n");
            }
	    }
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
	    return getLabel();
	}
	
	private boolean update() {
	    boolean ret = true;
		Matcher matcherQualifier = QualifierPattern.matcher(fText);
		fQualifier = "";
		fTitle = "";
        List<HemlElement> children = fChildren != null ? new ArrayList<>(Arrays.asList(fChildren)) : new ArrayList<>();
		if (!matcherQualifier.find()) ret = false;
		else {
            fQualifier = matcherQualifier.group(1);
            if (matcherQualifier.groupCount() > 2) {
                fTitle = matcherQualifier.group(3);
                if (fTitle != null) fTitle = fTitle.trim();
            }
            int currentIdx = 0;
            if (fQualifier.startsWith("#")) {
                Matcher endCode = CommentBlockEndPattern.matcher(fText);
                if (endCode.find()) {
                    fText = fText.substring(0, endCode.end());              
                }
                else ret = false;
            }
            else if (fQualifier.startsWith("!")) {
                Matcher endCode = CodeBlockEndPattern.matcher(fText);
                if (endCode.find()) {
                    fText = fText.substring(0, endCode.end());              
                }
                else ret = false;
            }
            else {
                int offsetClose = -1;
                int offsetOpen = -1;
                
                // need to substring to have pattern with ^ working well
                int currentOffset = matcherQualifier.start(1) + 1;
                do {
                    String currentText = fText.substring(currentOffset);
                    Matcher endBlockMatcher = EndBlockPattern.matcher(currentText);
                    matcherQualifier = QualifierPattern.matcher(currentText);
                    offsetClose = endBlockMatcher.find() ? endBlockMatcher.start(1) : -1;
                    // search next qualifier.
                    if (matcherQualifier.find() && (offsetOpen = matcherQualifier.start(1) - 1) < offsetClose) { //there is a child
                        HemlElement newChild = null;
                        long subOffset = fStartIndex + currentOffset + offsetOpen;
                        String subText = currentText.substring(offsetOpen);
                        if (currentIdx < children.size()) {
                            newChild = children.get(currentIdx);
                            newChild.update(subText, subOffset);
                        }
                        else {
                            newChild = new HemlElement(subText, this, subOffset);
                            children.add(newChild);
                        }
                        currentIdx++;
                        currentOffset += offsetOpen + newChild.getText().length();
                    } else break;
                } while(offsetClose != -1 && currentOffset < fText.length());   
                if (offsetClose != -1 && currentOffset + offsetClose + 1 < fText.length()) {
                    fText = fText.substring(0, currentOffset + offsetClose + 1);
                }       
            }
            if (currentIdx < children.size()) children = children.subList(0, currentIdx);
		}
        fChildren = children.stream().toArray(HemlElement[]::new);
		return ret;
	}
	
}
