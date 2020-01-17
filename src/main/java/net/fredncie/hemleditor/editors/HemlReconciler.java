package net.fredncie.hemleditor.editors;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class HemlReconciler extends PresentationReconciler implements IPropertyChangeListener {

    private final Token tagToken = new Token(null);
    private final Token parameterToken = new Token(null);
    private final Token commentToken = new Token(null);
    private final Token italicToken = new Token(null);
    private final Token kwToken = new Token(null);
    private final Token emToken = new Token(null);
    
    private ITheme fCurrentTheme;
    private ISourceViewer fSourceViewer;
    
    public HemlReconciler() {
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        fCurrentTheme = themeManager.getCurrentTheme();
        fCurrentTheme.addPropertyChangeListener(this);

        loadTheme();
        
        RuleBasedScanner scanner= new RuleBasedScanner();
        IRule[] rules = {
            new EndOfLineRule("%%", commentToken),
            new SingleLineRule("{i ", "}", italicToken),
            new SingleLineRule("{kw ", "}", kwToken),
            new SingleLineRule("{em ", "}", emToken),
            new SingleLineRule("{", " ", tagToken),
            new SingleLineRule("%", "=", parameterToken),
            new SingleLineRule("}", " ", tagToken, '\\', true)
        };
        
        scanner.setRules(rules);
        DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);
        this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
    }
    
    public void setSourceViewer(ISourceViewer sourceViewer) {
        fSourceViewer = sourceViewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        loadTheme();
        
        if (fSourceViewer != null) fSourceViewer.invalidateTextPresentation();
    }
    
    private void loadTheme() {
        tagToken.setData(createTextAttribute("tagsColor"));
        parameterToken.setData(createTextAttribute("parametersColor"));
        commentToken.setData(createTextAttribute("commentsColor", SWT.BOLD));
        italicToken.setData(createTextAttribute("italicColor", SWT.ITALIC));
        kwToken.setData(createTextAttribute("kwColor", SWT.ITALIC));
        emToken.setData(createTextAttribute("emColor", SWT.ITALIC | SWT.BOLD));

        TextAttribute codeAttribute = createTextAttribute("codeColor");
        TextAttribute tableAttribute = createTextAttribute("tableColor");
        TextAttribute multilineCommentAttribute = createTextAttribute("commentsBlockColor", SWT.ITALIC);
        
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

    private TextAttribute createTextAttribute(String name) {
        return createTextAttribute(name, 0);
    }
    private TextAttribute createTextAttribute(String name, int style) {
        ColorRegistry colorRegistry = fCurrentTheme.getColorRegistry();
        Color color = colorRegistry.get("net.fredncie.hemleditor.preferences." + name);
        return style == 0 ? new TextAttribute(color) : new TextAttribute(color, null, style);
    }
}
