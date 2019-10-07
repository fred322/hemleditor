package hemleditor2.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class HemlConfiguration extends SourceViewerConfiguration {

	private HemlReconciler hemlReconciler = new HemlReconciler();
	private IContentAssistProcessor hemlAssistantProcessor = new HemlContentAssistProcessor();
	private ContentAssistant assistant = new ContentAssistant();

	public HemlConfiguration() {	
		this.assistant.setContentAssistProcessor(hemlAssistantProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		List<String> types = new ArrayList<>(Arrays.asList(HemlPartitionScanner.TYPES));
		types.add(IDocument.DEFAULT_CONTENT_TYPE);
		return types.stream().toArray(String[]::new);
	}
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return this.hemlReconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		this.assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return this.assistant;
	}

}