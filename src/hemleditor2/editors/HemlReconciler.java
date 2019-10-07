package hemleditor2.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class HemlReconciler extends PresentationReconciler {

    private final TextAttribute tagAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(0,128, 0)));
    private final TextAttribute parameterAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(128, 0, 128)));
    private final TextAttribute commentAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(95,95,95)), null, SWT.BOLD);
    private final TextAttribute italicAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 0, 0)), null, SWT.ITALIC);
    private final TextAttribute kwAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(255, 0, 0)), null, SWT.ITALIC);
    private final TextAttribute emAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 0, 0)), null, SWT.ITALIC | SWT.BOLD);

    private final TextAttribute codeAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(255, 0, 128)));
    private final TextAttribute tableAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(157, 0, 0)));
    private final TextAttribute multilineCommentAttribute = new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 0, 255)), null, SWT.ITALIC);
    
    public HemlReconciler() {
        RuleBasedScanner scanner= new RuleBasedScanner();
        IRule[] rules = {
            new EndOfLineRule("%%", new Token(commentAttribute)),
            new SingleLineRule("{i", "}", new Token(italicAttribute)),
            new SingleLineRule("{kw", "}", new Token(kwAttribute)),
            new SingleLineRule("{em", "}", new Token(emAttribute)),
            new SingleLineRule("{", " ", new Token(tagAttribute)),
            new SingleLineRule("%", "=", new Token(parameterAttribute)),
            new SingleLineRule("}", " ", new Token(tagAttribute), '\\', true)
        };
        
        scanner.setRules(rules);
        DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);
        this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        
        NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(tableAttribute);
		this.setDamager(ndr, HemlPartitionScanner.TABLE_TYPE);
		this.setRepairer(ndr, HemlPartitionScanner.TABLE_TYPE);
		
		ndr = new NonRuleBasedDamagerRepairer(codeAttribute);
		this.setDamager(ndr, HemlPartitionScanner.CODE_TYPE);
		this.setRepairer(ndr, HemlPartitionScanner.CODE_TYPE);
		
		ndr = new NonRuleBasedDamagerRepairer(multilineCommentAttribute);
		this.setDamager(ndr, HemlPartitionScanner.COMMENT_TYPE);
		this.setRepairer(ndr, HemlPartitionScanner.COMMENT_TYPE);
    }
}
