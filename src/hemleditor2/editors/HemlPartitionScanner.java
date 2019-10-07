package hemleditor2.editors;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class HemlPartitionScanner extends RuleBasedPartitionScanner {
	public static final String CODE_TYPE = "__code_type";
	public static final String TABLE_TYPE = "__table_type";
	public static final String COMMENT_TYPE = "__comment_type";
	public static final String[] TYPES = { CODE_TYPE, TABLE_TYPE, COMMENT_TYPE };
	
	public HemlPartitionScanner() {
		setPredicateRules(new IPredicateRule[]{
				new MultiLineRule("{?", "}", new Token(TABLE_TYPE)),
				new MultiLineRule("{!", "!}", new Token(CODE_TYPE)),
				new MultiLineRule("{#", "#}", new Token(COMMENT_TYPE))
		});
	}
}
