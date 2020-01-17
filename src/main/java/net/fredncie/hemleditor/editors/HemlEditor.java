package net.fredncie.hemleditor.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class HemlEditor extends TextEditor implements ISelectionChangedListener {

	private HemlContentOutlinePage fOutlinePage;
	private ProjectionAnnotationModel fAnnotationModel;
	private Annotation[] fCurrentAnnotations;
	private HemlElement fMainHemlElement;
    private boolean fUpdating = false;
    private String fDocContentForUpdate = null;
    private final Object fMutexDocContent = new Object();
	
	public HemlEditor() {
		super();
		setSourceViewerConfiguration(new HemlConfiguration());
		HemlDocumentProvider hemlDocProvider = new HemlDocumentProvider();
		hemlDocProvider.setDocumentListener(new IDocumentListener() {
			@Override
			public void documentChanged(DocumentEvent arg0) {
				updateHelpers(arg0.getDocument());
			}
			@Override
			public void documentAboutToBeChanged(DocumentEvent arg0) {
			}
		});
		setDocumentProvider(hemlDocProvider);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
	    super.init(site, input);
	    IDocument document = getDocumentProvider().getDocument(input);
        updateHelpers(document);
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (fOutlinePage == null) fOutlinePage = new HemlContentOutlinePage();
			IEditorInput input = this.getEditorInput();
			IDocument document = this.getDocumentProvider().getDocument(input);
			updateHelpers(document);
			fOutlinePage.addSelectionChangedListener(this);
			this.getSelectionProvider().addSelectionChangedListener(fOutlinePage);
			
			return (T) fOutlinePage;
		}
		return super.getAdapter(adapter);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		// implementation de la partie folding de code
		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
		ProjectionSupport projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();
		
		//turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);
		fAnnotationModel = viewer.getProjectionAnnotationModel();
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		viewer.getSelectionProvider().addSelectionChangedListener(this);
		return viewer;
	}
	

	@Override
	public void selectionChanged(SelectionChangedEvent arg0) {
	    if (arg0.getSource() instanceof ProjectionViewer) {
	        if (fMainHemlElement != null && arg0.getSelection() instanceof TextSelection) {
	            int offsetSearch = Math.max(0, ((TextSelection) arg0.getSelection()).getOffset() - 1);
	            HemlElement child = fMainHemlElement.getChild(offsetSearch);
	            if (child != null) {
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
                            int newOffet = (int)child.getOffset();
	                        setHighlightRange(newOffet, 1, false);
	                    }
	                });               
	            }
	        }
	    }
	    else if (arg0.getSelection() instanceof TreeSelection) {
            Object element = ((TreeSelection) arg0.getSelection()).getFirstElement();
            if (element instanceof HemlElement) {
                HemlElement hemlElement = (HemlElement) element;
                this.selectAndReveal((int)hemlElement.getOffset(), 1);
            }
        }
	}
	
	private void updateHelpers(IDocument document) {
        synchronized (fMutexDocContent) {
            fDocContentForUpdate = document.get();            
        }
        
        if (!fUpdating) {
            fUpdating = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateHelpers2();
                }
            }).start();
        }
	}
	
	private void updateHelpers2() {
        String documentContent = null;
        synchronized (fMutexDocContent) {
            if (fDocContentForUpdate != null) {
                documentContent = fDocContentForUpdate;
                fDocContentForUpdate = null;
            }
        }
        if (documentContent != null) {            
            if (fMainHemlElement == null) fMainHemlElement = HemlElement.create(documentContent);
            else fMainHemlElement.update(documentContent);
            if (fOutlinePage != null) fOutlinePage.setInput(fMainHemlElement);
            updateFoldingStructure(fMainHemlElement);
            updateHelpers2();
        }
        else fUpdating = false;        
	}
	
	private void updateFoldingStructure(HemlElement mainHeml) {
		if (mainHeml != null) {
			List<Position> positions = new ArrayList<Position>();
			mainHeml.generatePosition(positions);
			
			Map<Annotation, Position> mapAnnotations = new HashMap<>();
			for (Position pos : positions) {
				ProjectionAnnotation annotation = new ProjectionAnnotation();
				mapAnnotations.put(annotation, pos);
			}
			Annotation[] annotations = mapAnnotations.keySet().stream().toArray(Annotation[]::new);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					fAnnotationModel.modifyAnnotations(fCurrentAnnotations, mapAnnotations, null);
					fCurrentAnnotations = annotations;					
				}
			});
		}
	}
	
}
