package cz.cuni.mff.d3s.nprg044.twitter.editor.simple;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

public class SimpleMultiPageEditor extends MultiPageEditorPart {
	
	private TextEditor textEditor;
	private ListViewer listViewer;

	@Override
	protected void createPages() {
		// create page 0
		createPage0();
		createPage1();
	}
	
	private void createPage0() {		
		try {
			textEditor = new TextEditor();
			int index = addPage(textEditor, getEditorInput()) ;
			setPageText(index, "Editor");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void createPage1() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		
		listViewer = new ListViewer(composite);
		int index = addPage(composite);
		setPageText(index, "Preview");		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		textEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		textEditor.doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 1) {
			String editorText = textEditor.getDocumentProvider().getDocument(getEditorInput()).get();
			StringTokenizer tokenizer = new StringTokenizer(editorText, "\t\n\r\f");
						
			listViewer.getList().removeAll();
			int i = 0;
			while (tokenizer.hasMoreTokens()) {
				listViewer.add(i+ ": " +tokenizer.nextToken());
				i++;
			}
		}
	}
	
}
