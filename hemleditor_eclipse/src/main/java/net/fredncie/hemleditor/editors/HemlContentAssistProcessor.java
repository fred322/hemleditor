package net.fredncie.hemleditor.editors;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
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
	private static final String EDITION = "edition";
	private static final String REFERENCE = "ref";
	private static final String DEFINITION = "def";
	private static final String REQUIREMENT = "req";
	private static final String TH = "th";
	private static final Set<String> INLINE_TAGS = new HashSet<>();
    
    private HemlAutocompletionProvider autoCompletionProvider = new HemlAutocompletionProvider();
    
    static {
        INLINE_TAGS.addAll(Arrays.asList(TITLE, REFERENCE_1, DATE, VERSION, REVISION, COPYRIGHT, 
                ABSTRACT, REFERENCE, EDITION, DEFINITION, TH, REQUIREMENT, KEYWORDS, "kw", "i" ));
    }
    
    public HemlContentAssistProcessor() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("DefaultHeml.xsd")) {
            if (stream != null) {
                autoCompletionProvider.loadXsd(stream);
            }   
            else {
                System.err.println("Cannot load DefaultHeml.xsd");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        String text = viewer.getDocument().get();
        
        HemlElement element = HemlElement.create(text);
        if (element != null) element = element.getChild(offset);
        
        String tagStart = "{";
        String paramStart = "%";

        ICompletionProposal[] proposals = null;
        int lastSpace = Math.max(text.lastIndexOf("\t", offset - 1), text.lastIndexOf(" ", offset - 1));
        String startText = text.substring(lastSpace + 1, offset);
        if (!startText.contains(" ")) {
            if (startText.startsWith(tagStart)) {
                HemlElement parent = element;
                if (startText.length() > 1) {
                    parent = element != null ? element.getParent() : null;
                }
                String startTagName = startText.substring(tagStart.length());

                String indentStr = "";
            	int startTag = offset - startText.length(); 
                int startLine = text.lastIndexOf('\n', offset - 1);
                if (startLine > 0 && startTag > startLine) {
                    indentStr = text.substring(startLine + 1, startTag);
                }
                final String finalIndentStr = indentStr;
                String[] tags = parent != null ? autoCompletionProvider.getElements(parent) : new String[]{ DOCUMENT };
                if (tags != null) {
                    proposals = Arrays.asList(tags).stream()
                            .filter(tag -> tag.startsWith(startTagName))
                            .map(tag -> createCompletionProposal(tag, startTagName.length(), offset, finalIndentStr))
                            .toArray(CompletionProposal[]::new);                	
                }                
            }
            else if (startText.startsWith(paramStart)) {
                String startParamName = startText.substring(paramStart.length());
                String[] params = autoCompletionProvider.getAttributes(element);
                if (params != null)
                {
                    proposals = Arrays.asList(params).stream()
                            .filter(param -> param.startsWith(startParamName))
                            .map(param -> createParamCompletionProposal(param, startParamName.length(), offset))
                            .toArray(CompletionProposal[]::new);
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
    	if (!INLINE_TAGS.contains(tag)) toShow += "\n" + indentation;
    	toShow += "}";
    	return new CompletionProposal(toShow, offset - tagOffset - 1, tagOffset + 1, tag.length() + 2);
    }

    private ICompletionProposal createParamCompletionProposal(String tag, int tagOffset, int offset) {
    	// String left = tag.substring(tagOffset);
    	String toShow = "%" + tag + "=";
    	return new CompletionProposal(toShow, offset - tagOffset - 1, tagOffset + 1, tag.length() + 2);
    }
}
