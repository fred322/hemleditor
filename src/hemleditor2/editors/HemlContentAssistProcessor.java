package hemleditor2.editors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class HemlContentAssistProcessor implements IContentAssistProcessor {

	private static final String DOCUMENT = "document";
	private static final String TITLE = "title";
	private static final String REFERENCE_1 = "reference";
	private static final String DATE = "date";
	private static final String VERSION = "version";
	private static final String REVISION = "revision";
	private static final String ABSTRACT = "abstract";
	private static final String KEYWORDS = "keywords";
	
	private static final String COPYRIGHT = "copyright";
	private static final String AUTHOR = "author";
	private static final String HISTORY = "history";
	private static final String EDITION = "edition";
	private static final String SECTION = "section";
	private static final String REFERENCES = "references";
	private static final String REFERENCE = "ref";
	private static final String DEFINITIONS = "definitions";
	private static final String DEFINITION = "def";
	private static final String REQUIREMENT = "req";
	private static final String CODE = "code";
	private static final String TABLE = "table";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String CHECK = "check";
	private static final String OPERATION = "operation";
	private static final String ASSERT = "assert";
	private static final String NOTE = "note";
	private static final String FIGURE = "fig";
	
	
    private static final Map<String, String[]> MAP_PARAMS = new HashMap<>();
    private static final Map<String, String[]> MAP_TAGS = new HashMap<>();
    private static final Set<String> INLINE_TAGS = new HashSet<>();
    
    static {
        MAP_PARAMS.put(COPYRIGHT, new String[]{ "year", "holder" });
        MAP_PARAMS.put(AUTHOR, new String[]{ "sigle" });
        MAP_PARAMS.put(EDITION, new String[]{ "version", "date" });
        MAP_PARAMS.put(SECTION, new String[]{ "title" });
        MAP_PARAMS.put(REFERENCES, new String[]{ "title", "id" });
        MAP_PARAMS.put(REFERENCE, new String[]{ "id", "author", "edition", "ref" });
        MAP_PARAMS.put(DEFINITIONS, new String[]{ "title" });
        MAP_PARAMS.put(DEFINITION, new String[]{ "entry" });
        MAP_PARAMS.put(REQUIREMENT, new String[]{ "id" });
        MAP_PARAMS.put(CODE, new String[]{ "language", "title" });
        MAP_PARAMS.put(CHECK, new String[]{ "id", "title", "xref" });
        MAP_PARAMS.put(NOTE, new String[]{ "type", "title" });
        MAP_PARAMS.put(FIGURE, new String[]{ "title", "src" });
        MAP_PARAMS.put(TABLE, new String[]{ "title" });
        
        MAP_TAGS.put(DOCUMENT, new String[]{ TITLE, REFERENCE_1, DATE, VERSION, REVISION, COPYRIGHT , AUTHOR, ABSTRACT, KEYWORDS, HISTORY, SECTION });
        MAP_TAGS.put(HISTORY, new String[]{ EDITION });
        MAP_TAGS.put(SECTION, new String[]{ SECTION, REFERENCES, DEFINITIONS, REQUIREMENT, CODE, TABLE, CHECK, NOTE, FIGURE });
        MAP_TAGS.put(REFERENCES, new String[]{ REFERENCE });
        MAP_TAGS.put(DEFINITIONS, new String[]{ DEFINITION });
        MAP_TAGS.put(TABLE, new String[]{ TR });
        MAP_TAGS.put(TR, new String[]{ TH });
        MAP_TAGS.put(CHECK, new String[]{ OPERATION, ASSERT });
        MAP_TAGS.put(OPERATION, new String[]{ CODE });
        MAP_TAGS.put(ASSERT, new String[]{ REQUIREMENT });
        
        INLINE_TAGS.addAll(Arrays.asList(TITLE, REFERENCE_1, DATE, VERSION, REVISION, COPYRIGHT, ABSTRACT, REFERENCE, EDITION, DEFINITION, TH, REQUIREMENT, KEYWORDS ));
    }
    
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        String text = viewer.getDocument().get();
        String tagStart = "{";
        String paramStart = "%";

        ICompletionProposal[] proposals = null;
        int lastSpace = Math.max(text.lastIndexOf("\t", offset - 1), text.lastIndexOf(" ", offset - 1));
        String startText = text.substring(lastSpace + 1, offset);
        if (!startText.contains(" ")) {
            if (startText.startsWith(tagStart)) {
            	String parent = findParent(text, lastSpace);
                String startTagName = startText.substring(tagStart.length());

                String indentStr = "";
            	int startTag = offset - startText.length(); 
                int startLine = text.lastIndexOf('\n', offset - 1);
                if (startLine > 0 && startTag > startLine) {
                    indentStr = text.substring(startLine + 1, startTag);
                }
                final String finalIndentStr = indentStr;
                String[] tags = parent != null ? MAP_TAGS.get(parent) : new String[]{ DOCUMENT };
                if (tags != null) {
                    proposals = Arrays.asList(tags).stream()
                            .filter(tag -> tag.startsWith(startTagName))
                            .map(tag -> createCompletionProposal(tag, startTagName.length(), offset, finalIndentStr))
                            .toArray(CompletionProposal[]::new);                	
                }                
            }
            else if (startText.startsWith(paramStart)) {
                String startParamName = startText.substring(paramStart.length());
                int startParent = text.lastIndexOf('{', offset);
                if (startParent != -1)
                {
                    String parent = text.substring(startParent + 1, offset);
                    parent = parent.split(" ")[0];
                    String[] params = MAP_PARAMS.get(parent);
                    if (params != null)
                    {
                        proposals = Arrays.asList(params).stream()
                                .filter(param -> param.startsWith(startParamName))
                                .map(param -> createParamCompletionProposal(param, startParamName.length(), offset))
                                .toArray(CompletionProposal[]::new);
                    }
                    
                }
            }
        }
        return proposals != null ? proposals : new ICompletionProposal[0];
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '{', '%' }; //NON-NLS-1
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }
    
    private ICompletionProposal createCompletionProposal(String tag, int tagOffset, int offset, String indentation) {
    	// String left = tag.substring(tagOffset);
    	String toShow = "{" + tag + " ";
    	if (!INLINE_TAGS.contains(tag))
    		toShow += "\n" + indentation;
    	toShow += "}";
    	return new CompletionProposal(toShow, offset - tagOffset - 1, tagOffset + 1, tag.length() + 2);
    }

    private ICompletionProposal createParamCompletionProposal(String tag, int tagOffset, int offset) {
    	// String left = tag.substring(tagOffset);
    	String toShow = "%" + tag + "=";
    	return new CompletionProposal(toShow, offset - tagOffset - 1, tagOffset + 1, tag.length() + 2);
    }
    
    
    private String findParent(String text, int offset) {
    	String ret = null;
    	if (offset > 0) {
    		int bracketNum = 0;
    		String toAnalyze = text.substring(0, offset);
    		int currentIdx = toAnalyze.length() - 1;
    		for (; currentIdx >= 0; currentIdx--) {
    			if (toAnalyze.charAt(currentIdx) == '}')
    				bracketNum++;
    			else if (toAnalyze.charAt(currentIdx) == '{') {
    				if (bracketNum == 0)
    					break;
    				else
    					bracketNum--;	
    			}
    		}
    		
    		if (currentIdx >= 0) {
    			int space = text.indexOf(" ", currentIdx);
    			if (space != -1) 
    			{
    				ret = text.substring(currentIdx + 1, space);
    			}
    		}
    		/*
        	String noBrackets = "[^\\{\\}]*"; 
        	Pattern pattern = Pattern.compile("\\{([^\\s]+)" + noBrackets + "(?:" + noBrackets + "\\{" + noBrackets + "\\})*" + noBrackets + "$");
        	Matcher matcher = pattern.matcher(text.substring(0, offset));
        	if (matcher.find()) {
        		ret = matcher.group(1);
        	}    		*/
    	}
    	return ret;
    }
}
