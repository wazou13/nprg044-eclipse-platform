package cz.cuni.mff.d3s.nprg044.twitter.editor.text.outline;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class MarkdownTextOutlinePage extends ContentOutlinePage  implements ITextInputListener, CaretListener, ISelectionChangedListener {
	
	private IDocument input;
	
	private class SimpleLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {			
			if (element instanceof ITypedRegion) {
				ITypedRegion region = (ITypedRegion) element;
				
				try {
					return input.get(region.getOffset(), region.getLength());
				} catch (BadLocationException e) {					
				}
			}
			return super.getText(element);			
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer treeViewer = getTreeViewer();
		treeViewer.setContentProvider(new MarkdownSyntaxContentProvider());
		treeViewer.setLabelProvider(new SimpleLabelProvider());
//		treeViewer.addSelectionChangedListener(this)
		
		treeViewer.setInput(this.input);
		
		getSite().setSelectionProvider(treeViewer);
	}
	
	public void setInput(IDocument element) {
		this.input = element;
		if (getTreeViewer()!=null) {
			getTreeViewer().setInput(element);		
		}
	}

	@Override
	public void inputDocumentAboutToBeChanged(IDocument oldInput,
			IDocument newInput) {		
	}

	@Override
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		setInput(newInput);		
	}

	@Override
	public void caretMoved(CaretEvent event) {
		try {
			ITypedRegion region = input.getPartition(event.caretOffset);
			
			if (getTreeViewer()!=null) {
				getTreeViewer().setSelection(new StructuredSelection(region));
//				getTreeViewer().reveal(region);
				getTreeViewer().expandToLevel(region, TreeViewer.ALL_LEVELS);
			}
			
		} catch (BadLocationException e) {		
			e.printStackTrace();
		}		
	}	
}
