# Introduction #


# Eclipse Editors #

Editors share the same concepts as views, but they are primarily intended for data manipulation.

```
IWorkbenchPart
 |
 +- IEditorPart
  |
  +- EditorPart
   |
   +- MultiPageEditorPart
   +- TextEditor
```

An editor implements the `IEditorPart` interface. From the implementation perspective, it is better to inherit from the class `EditorPart` that provides a basic infrastructure.
It is necessary to implement:
  * `init(IEditorSite site, IEditorInput input)` - checks supported input, registers listeners, etc.
  * override `createPartControl(Composite parent)` - creates the editor control (e.g., SWT widgets `Text`, `TextViewer`, `SourceViewer`).

Similarly to views, the class `IEditorSite` serves as a communication channel between the editor and the underlying Eclipse workbench (which registers action bars and context menu). See the previous lecture about views.

The class `IEditorInput` represents the input which will be managed by the editor. It can be a file (`IFileEditorInput`), URI (`IURIEditorInput`), Eclipse path (`IPathEditorInput`), or a user defined input element.

## Simple multi-page editor example ##

  * create a new plug-in `cz.cuni.mff.d3s.nprg044.twitter.editor`
  * add the following plug-ins into _Dependencies_:
    * `org.eclipse.ui.editors`
    * `org.eclipse.jface.text`
  * add _Extension_ `org.eclipse.ui.editors` and add a new editor
    * _ID_ = `cz.cuni.mff.d3s.nprg044.twitter.editors.SimpleMultiPageEditor`

  * create the editor implementation `SimpleMultiPageEditor` extending the class `MultiPageEditorPart`
    * the implementation should have 2 pages:
      1. page should contain a classical editor - use the `TextEditor` widget
      1. page should show a list of lines - use the `ListViewer` widget
    * the method `createPage` creates two pages with the abovementioned controls:
      * the editor input is handled automatically by the superclass
      * to access parent SWT container use the method `getContainer()`
      * numbering of pages is 0-based

```
private TextEditor textEditor;
private ListViewer listViewer;

// this method creates visual representation of all pages from various SWT widgets and controls
@Override
protected void createPages() {
	createPage0();
	createPage1();
}

// this page contains a standard text editor
private void createPage0() {	
	try {
		textEditor = new TextEditor();
			
		// add new page to the multi-page editor
		int index = addPage(textEditor, getEditorInput());
			
		// set the page label
		setPageText(index, "Editor");
	} 
	catch (PartInitException e) {
		e.printStackTrace();
	}
}

// this page shows a list of lines
private void createPage1() {
	// there will be just the list viewer in the page so layout does not really matter 
	Composite composite = new Composite(getContainer(), SWT.NONE);
	FillLayout layout = new FillLayout();
	composite.setLayout(layout);
		
	listViewer = new ListViewer(composite);
		
	int index = addPage(composite);		
	setPageText(index, "Preview");		
}
```

  * implement save methods by delegating them on the `textEditor` widget
```
@Override
public void doSave(IProgressMonitor monitor) {
	// tell editor to save its content
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
```

  * fill the `listViewer` widget only if it is shown:
```
// notification that some page (tab) has been activated (displayed)
@Override
protected void pageChange(int newPageIndex) {
	super.pageChange(newPageIndex);
		
	if (newPageIndex == 1) {
		// our list has been activated -> fill it with current data
			
		// get the whole content of the text editor
		String editorText = textEditor.getDocumentProvider().getDocument(getEditorInput()).get();
			
		StringTokenizer tokenizer = new StringTokenizer(editorText, "\t\n\r\f");
			
		// remove old content
		listViewer.getList().removeAll();
			
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			listViewer.add(i+ ": " +tokenizer.nextToken());
			i++;
		}
	}
}
```


## Text Editor improvements ##
The next step is to improve common implementation of `TextEditor` to support the _markdown_ syntax.
The _markdown_ syntax supports the following marks:
  * `= HEADING 1 =`
  * `== HEADING 2 ==`
  * `* bold text *`
  * `_ italics text _`

For example:
```
= Heading 1=
== Sub-heading ==
This is a normal text followed by text in *bold*.

_And this is a multiline
italics text_.
```

In the following example, we will improve the text editor (the `TextEditor` class) to support the _markdown_ syntax.
The class `TextEditor` is highly configurable and supports:
  * syntax highlight
  * text partitioning
  * hover
  * content assist
  * separation between text in the editor and the edited model
  * annotation
  * ...

### Creating the editor ###

  * add the extension `org.eclipse.ui.editors` and a new editor:
    * _ID_ = `cz.cuni.mff.d3s.nprg044.twitter.editor.MarkdownTextEditor`
    * _extension_ = `markdown`
  * implement the class `cz.cuni.mff.d3s.nprg044.twitter.editor.text.MarkdownTextEditor` extending the class `TextEditor`
```
public class MarkdownTextEditor extends TextEditor {
	
	private ColorManager colorManager;
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		colorManager = new ColorManager();
		
		// setup configuration of some graphical presentation aspects of the editor's raw content
		setSourceViewerConfiguration(new MarkdownTextEditorConfiguration(colorManager));
		
		// setup document provider
		setDocumentProvider(new MarkdownTextDocumentProvider());
	}	
		
	@Override
	public void dispose() {
		colorManager.dispose();
		
		super.dispose();
	}
}
```

| _Re-use the `ColorManager` class and the `IMarkdownTextColorConstants` interface from the given source code. The `ColorManager` manages creation of instances of the `Color` class according to a given RGB value._|
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

There are two important calls - `setDocumentProvider` and `setSourceViewerConfiguration`.

### Document Provider ###
The document provider implements the interface `IDocumentProvider` which provides a mapping between the editor input (i.e., `IEditorInput`) and its presentation model (what is shown in the text editor) represented by the interface `IDocument`.

The interface `IDocument` serves to maintain the text shown in the editor control - to access lines, characters, update the given position, replace the text, etc.

Document provider can also provide information about hidden annotations (i.e., the _annotation model_). See http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/guide/editors_annotations.htm

| _The document provider can be also registered via the extension point `org.eclipse.ui.editors.documentProviders`_|
|:-----------------------------------------------------------------------------------------------------------------|

Implement the class `MarkdownTextDocumentProvider`:
```
public class MarkdownTextDocumentProvider extends FileDocumentProvider {
	/**
	 * Return an IDocument object that allows to work with the actual text representation
	 * of the document shown in the editor widget (control) - to access lines, chars, 
	 * update the current position, replace text fragments, and so on.
	 */
	@Override
	protected IDocument createDocument(Object element) throws CoreException {		
		IDocument document = super.createDocument(element);
		
		// register the document partitioner
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new MarkdownTextPartitionScanner(), 
					MarkdownTextPartitionScanner.LEGAL_CONTENT_TYPES
				);
			
			// connect the partitioner with the document
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
				
		return document;
	}
}
```

#### Partitions ####
The partitioner (`IDocumentPartitioner`) is responsible for dividing the document into non-overlapping regions called partitions. Partitions (represented by `ITypedRegion`) are useful for treating different sections of the document differently with respect to features like syntax highlighting or formatting. Each region has an associated _content type_.
Multiple partitioning strategies can be registered (see interface `IDocumentExtension3`).

It is recommended to use the rule-based partitioning via `FastPartitioner` which needs a scanner that recognizes different partitions.
In our case, we have four partitions that follow the markdown syntax rules (heading1, heading2, bold, italics):
```
public class MarkdownTextPartitionScanner extends RuleBasedPartitionScanner {
	public static final String MARKDOWN_H1 = "__markdown_h1";
	public static final String MARKDOWN_H2 = "__markdown_h2";
	public static final String MARKDOWN_BOLD = "__markdown_bold";
	public static final String MARKDOWN_ITALICS = "__markdown_italics";
	
	public static final String[] LEGAL_CONTENT_TYPES = {MARKDOWN_H1, MARKDOWN_H2, MARKDOWN_BOLD, MARKDOWN_ITALICS};
	
	public static final int NUMBER_OF_RULES = 4;
	
	public MarkdownTextPartitionScanner() {
		IToken heading1 = new Token(MARKDOWN_H1);
		IToken heading2 = new Token(MARKDOWN_H2);
		IToken bold = new Token(MARKDOWN_BOLD);
		IToken italics = new Token(MARKDOWN_ITALICS);
		
		// the order of rules is very important !!
		IPredicateRule[] rules = new IPredicateRule[NUMBER_OF_RULES];
		rules[0] = new PatternRule("==", "==", heading2, (char) 0, false);
		rules[1] = new PatternRule("=", "=", heading1, (char) 0, false);		
		rules[2] = new MultiLineRule("*", "*", bold);			
		rules[3] = new MultiLineRule("_", "_", italics);
		
		setPredicateRules(rules);
	}
}
```

  * `IToken` represents a token returned by the parser if the given rule matches.
  * There are various types of rules which match whitespaces, numbers, words, patterns, ...
  * In this example the rule order is very important, because the scanner returns the first successful match. Hence, the rule for `==` has to registered before the rule for recognizing `=`.
    * Try to change the order.

|_Open question is if the partitions can be nested or recursively defined. It seems that they cannot be nested._|
|:--------------------------------------------------------------------------------------------------------------|

### Source Viewer Configuration ###
The class `TextEditor` internally uses the `SourceViewer` JFace widget (see the interface `ISourceViewer`). It can be configured via calling `setSourceViewerConfiguration` which accepts an instance of the class `SourceViewerConfiguration`.

  * create the class `MarkdownTextEditorConfiguration` extending `SourceViewerConfiguration`
  * register the supported content types
    * there are 4 content types from the partitioner
    * the rest of the text is represented by IDocument.DEFAULT\_CONTENT\_TYPE (the default content type)
```
public class MarkdownTextEditorConfiguration extends SourceViewerConfiguration {

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
				MarkdownTextPartitionScanner.MARKDOWN_H1,
				MarkdownTextPartitionScanner.MARKDOWN_H2,
				MarkdownTextPartitionScanner.MARKDOWN_BOLD,
				MarkdownTextPartitionScanner.MARKDOWN_ITALICS 
			};
	}
}
```

#### Syntax coloring ####
It is necessary to define scanners which return tokens containing text attributes - see the class `TextAttribute`.
The coloring itself is based on the reconciler (`PresentationReconciler`) which detects damaged text (`IPresentationDamager`) and repairs it (`IPresentationRepairer`).

|Eclipse Help: Damagers (`IPresentationDamager`) determine the region of the document's presentation which must be rebuilt due to a change of the document content. A presentation damager is assumed to be specific to a particular document content type (or region). It must be able to return a damaged region that is a valid input for the presentation repairer (`IPresentationRepairer`). A repairer must be able to derive all of the information it needs from the damaged region in order to successfully describe the repairs that are needed for a particular content type. Reconciling describes the overall process of maintaining the presentation of a document as changes are made in the editor. A presentation reconciler (IPresentationReconciler) monitors changes to the text through its associated viewer. It uses the document's regions to determine the content types affected by the change and notifies a damager that is appropriate for the affected content type. Once the damage is computed, it is passed to the appropriate repairer which will construct repair descriptions that are applied to the viewer to put it back in sync with the underlying content.|
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

In our example we use `DefaultDamagerRepairer` which is based on a scanner that returns tokens with text attributes for the given content type region to be repaired.
```

/**
 * Returns the PresentationReconciler object that is responsible for the proper
 * style attributes (presentation) of each document partition.
 * It manages the process of defining correct style attributes (color, etc) for
 * text partitions that are changed by the user, i.e. correct style for updated
 * partitions of the document content ("model").
 * It identifies the "damaged text" (without the proper style) in the document based 
 * on user's actions (typing, etc) and "repairs" it.
 */
@Override
public IPresentationReconciler getPresentationReconciler (ISourceViewer sourceViewer) {
	PresentationReconciler reconciler = new PresentationReconciler();
		
	DefaultDamagerRepairer dr = null;
		
	// creates the damager-repairer for the H1 content type (mark)
		// the scanner must define the correct style attributes for every possible token
		// that can appear inside text with the given content type
	dr = new DefaultDamagerRepairer(getMarkdownH1Scanner());		
	reconciler.setDamager(dr, MarkdownTextPartitionScanner.MARKDOWN_H1);
	reconciler.setRepairer(dr, MarkdownTextPartitionScanner.MARKDOWN_H1);

	/* ... */
	return reconciler;
}

private RuleBasedScanner markdownH1Scanner;

/**
 * A scanner that returns tokens that have proper text attributes for H1 content type.
 * The scanner says what style attributes (color, font) the tokens should have.
 * 
 * Here we define the same style for all possible text with the content type H1.
 */
private ITokenScanner getMarkdownH1Scanner() {
	if (markdownH1Scanner == null) {			
		markdownH1Scanner = new RuleBasedScanner() {
				{
					setDefaultReturnToken(new Token(new TextAttribute(
							colorManager.getColor(IMarkdownTextColorConstants.H1),
							colorManager.getColor(IMarkdownTextColorConstants.H1_BG),
							TextAttribute.UNDERLINE
						)));
				}
			};
	}
	return markdownH1Scanner;
}
```

#### Hover support ####
To support hovers in the text, add the following method to the implementation of `MarkdownTextEditorConfiguration`:
```
@Override
public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
	return new MarkdownTextHover();
}

/**
 * Instance of this class provide text to be shown in the hover popup windows.
 */
public class MarkdownTextHover implements ITextHover, ITextHoverExtension2 {
	// return information to be shown when the cursor is on the given region
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		try {
			return textViewer.getDocument().getPartition(hoverRegion.getOffset()).toString();
		} 
		catch (BadLocationException e) {
			return "No info because of " + e.getMessage();			
		}
	}

	// just an old version of the API method that returns only strings
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {		
		return getHoverInfo2(textViewer, hoverRegion).toString();
	}

	// returns the region object for a given position in the text editor
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		// if no text is selected then we return a region of the size 0 (a single character)
		return new Region(offset, 0);
	}
}
```

#### Content assist support ####
Different content assistants (e.g., for code completion) can be registered for each partition content-type:
```
@Override
public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	ContentAssistant assistant = new ContentAssistant();
	IContentAssistProcessor sharedProcessor = new MarkdownContentAssistProcessor();
		
	// define assist processor for each content type -> we use the same for all types here
	assistant.setContentAssistProcessor(sharedProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	assistant.setContentAssistProcessor(sharedProcessor, MarkdownTextPartitionScanner.MARKDOWN_H1);
	assistant.setContentAssistProcessor(sharedProcessor, MarkdownTextPartitionScanner.MARKDOWN_H2);
	assistant.setContentAssistProcessor(sharedProcessor, MarkdownTextPartitionScanner.MARKDOWN_BOLD);
	assistant.setContentAssistProcessor(sharedProcessor, MarkdownTextPartitionScanner.MARKDOWN_ITALICS);		
		
	assistant.setEmptyMessage("Sorry, no hint for you :-/");
	assistant.enableAutoActivation(true);
	assistant.setAutoActivationDelay(500);
	
	return assistant;
}
```

  * It is recommended to use default implementation `ContentAssistant` and to specify various `IContentAssistProcessor` objects:
```
public class MarkdownContentAssistProcessor implements IContentAssistProcessor {
	private static final String[] PROPOSALS = { "=", "==", "*", "_" };

	/**
	 * Return completion hints for the given offset.
	 * Here we always return all supported markup symbols.
	 */
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		
		for (int i = 0; i < PROPOSALS.length; i++) {
			result.add(new CompletionProposal(PROPOSALS[i], offset, 0, PROPOSALS[i].length()));			
		}
		
		return result.toArray(new ICompletionProposal[result.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	// completion hints triggered when the user types '='
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '=' };
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
}
```

#### Text manipulation ####
We want to support the _Ctrl+B_ shortcut to mark bold text in the editor:

  * add the extension `org.eclipse.ui.commands`
    * add a new command `cz.cuni.mff.d3s.nprg044.twitter.editor.MarkBoldCommand`
      * implement a _default handler_ `cz.cuni.mff.d3s.nprg044.twitter.editor.text.commands.MarkBoldHandler`
        * recognizes selection and encapsulates it with the `*` marks
```
public class MarkBoldHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		
		if (editorPart instanceof TextEditor) {
			// get current selection in the text editor
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			
			if (selection instanceof TextSelection) {
				TextEditor editor = (TextEditor) editorPart;
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());				
				TextSelection textSelection = (TextSelection) selection;
				try {
					// put selection into *...* (mark symbol for bold text)
					document.replace(textSelection.getOffset(), textSelection.getLength(), "*" + textSelection.getText()+ "*");
				}
				catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
}
```

Now it is necessary to associate the command with the key-binding:
  * add the extension `org.eclipse.ui.bindings`
    * _commandId_ = `cz.cuni.mff.d3s.nprg044.twitter.editor.MarkBoldCommand`
    * _sequence_ = `M1+B`
    * _schemeId_ = `org.eclipse.ui.defaultAcceleratorConfiguration`
    * _contextId_ =`org.eclipse.ui.textEditorScope`
      * context: we want to activate the key-binding only inside `TextEditor`

| _Interfaces `IDocumentProvider`, `IDocument`, `ISourceViewer` and other interfaces evolve during the development of Eclipse. Hence, there are often several extensions of the base interface, e.g., `IDocumentProviderExtension2`. All of them are typically implemented by a common base class (e.g., `SourceViewer` in case of the interface `ISourceViewer`)_|
|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

### Outline view support ###
To show the structure of the text (e.g., the structure of headings), the editor has to provide implementation of the `IContentOutlinePage` interface, which is then shown by the _Content Outline View_.
A typical way of providing the implementation is via the `IAdaptable` interface:

  * text editor `MarkdownTextEditor` implements an adapter for the `IContentOutlinePage` interface
    * override the `getAdapter` method:
```
	// page that will show the text structure (headings)
	private MarkdownTextOutlinePage outlinePage;
	
	/**
	 *  Returns an object of the IContentOutlinePage class that is associated with this editor.
	 *  The page is displayed inside the outline window.
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {		
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outlinePage == null) {
				outlinePage = new MarkdownTextOutlinePage();
				outlinePage.setInput(getDocumentProvider().getDocument(getEditorInput()));
			}	
			
			return outlinePage;
		} 
		
		return super.getAdapter(adapter);
	}
```
  * the class `MarkdownTextOutlinePage` implements the interface `IContentOutlinePage` via extending the support class `ContentOutlinePage`
    * override the method `createControl` and provide appropriate content provider and label provider
```
public class MarkdownTextOutlinePage extends ContentOutlinePage  {

	private IDocument input;
	
	private class SimpleLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {			
			if (element instanceof ITypedRegion) {
				ITypedRegion region = (ITypedRegion) element;
				
				try {
					return input.get(region.getOffset(), region.getLength());
				} 
				catch (BadLocationException e) {					
				}
			}
			return super.getText(element);			
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		// get tree viewer of this page
		TreeViewer treeViewer = getTreeViewer();
		
		treeViewer.setContentProvider(new MarkdownSyntaxContentProvider());
		treeViewer.setLabelProvider(new SimpleLabelProvider());
		
		treeViewer.setInput(this.input);
		
		// we must look for selections in the tree viewer so that we can show properties
		// of the selected node
		getSite().setSelectionProvider(treeViewer);
	}
	
	public void setInput(IDocument element) {
		this.input = element;
		if (getTreeViewer()!=null) {
			getTreeViewer().setInput(element);		
		}
	}
}
```

  * it extends the `ContentOutlinePage` class and overrides the method `createControl`
    * calls the super-method to create `TreeViewer`
    * uses the `getTreeViewer()` method to access the tree viewer
    * setup content and label providers via calling `TreeViewer.setContentProvider` and `TreeViewer.setLabelProvider`
    * set input via `TreeViewer.setInput`

  * implement the content provider `MarkdownSyntaxContentProvider` to show hierarchy of the level1 and level2 headings.
```
public class MarkdownSyntaxContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = {};
	private TreeViewer viewer;
	private IDocument document;

	private IDocumentListener documentListener = new IDocumentListener() {
		@Override
		public void documentChanged(DocumentEvent event) {
			if (!MarkdownSyntaxContentProvider.this.viewer.getControl().isDisposed()) {
				MarkdownSyntaxContentProvider.this.viewer.refresh();
			}
		}
		
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {					
		}
	};

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		
		if (oldInput instanceof IDocument) {
			document.removeDocumentListener(documentListener);
		}
		
		if (newInput instanceof IDocument) {
			document = (IDocument) newInput;
			document.addDocumentListener(documentListener);
		}		
	}

	@Override
	public Object[] getElements(Object inputElement) {
		IDocument document = getDocument(inputElement);
		if (document != null) {
			ArrayList<ITypedRegion> result = new ArrayList<ITypedRegion>();
			try {
				// get all partitions in the document
				ITypedRegion[] regions = document.computePartitioning(0, document.getLength());
				// headings of the type H1 are the top level elements
				for(ITypedRegion region : regions) {
					if (region.getType().equals(MarkdownTextPartitionScanner.MARKDOWN_H1)) {
						result.add(region);
					}
				}
			} 
			catch (BadLocationException e) {						
				e.printStackTrace();
			}
			return result.toArray();			
		}
		
		return EMPTY;		
	}

	private IDocument getDocument(Object inputElement) {
		if (inputElement instanceof IDocument) {
			return (IDocument) inputElement;
		}
		
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITypedRegion) {
			ITypedRegion parentRegion = (ITypedRegion) parentElement;
			
			ArrayList<ITypedRegion> result = new ArrayList<ITypedRegion>();
			
			if (parentRegion.getType().equals(MarkdownTextPartitionScanner.MARKDOWN_H1)) {
				ITypedRegion[] regions;
				try {
					// get all partitions in the given region (heading H1)
					regions = document.computePartitioning(parentRegion.getOffset()+parentRegion.getLength(), document.getLength()-parentRegion.getOffset()-parentRegion.getLength());
				
					// get all headings of the second level (H2) in the scope of the current first-level heading (H1)
					for(ITypedRegion h2region : regions) {
						if (h2region.getType().equals(MarkdownTextPartitionScanner.MARKDOWN_H2)) {
							result.add(h2region);
						}
						if (h2region.getType().equals(MarkdownTextPartitionScanner.MARKDOWN_H1)) {
							break;
						}
					}
					
					return result.toArray();
				} 
				catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}		
		return EMPTY;		
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITypedRegion) {
			ITypedRegion region = (ITypedRegion) element;
			if (region.getType().equals(MarkdownTextPartitionScanner.MARKDOWN_H1)) {
				return true;
			}			
		}
		return false;
	}

	@Override
	public void dispose() {		
	}
}
```

#### Content outline selection ####
If a node in the content outline is selected then the cursor should move to the corresponding section in the text editor.

  * in the `getAdapter` method, register a listener for selection of elements in the content outline page:
```
// register a listener for selection of elements in the content outline page
outlinePage.addSelectionChangedListener(new ISelectionChangedListener() {
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			// show and highlight the selected text region
			ITypedRegion region = (ITypedRegion) selection.getFirstElement();
			selectAndReveal(region.getOffset(), region.getLength());
			setHighlightRange(region.getOffset(), region.getLength(), true);
		}						
	}
});
```

#### Editor position in content outline ####
We want to show the appropriate content outline node highlighted if the editor cursor is placed on the location of some heading.
The solution is to implement `CaretListener` (SWT listener) in `MarkdownTextOutlinePage`:
```
@Override
public void caretMoved(CaretEvent event) {
	try {
		// if the current position of the cursor in the editor is at some heading
		// then highlight the corresponding outline node
		ITypedRegion region = input.getPartition(event.caretOffset);
			
		if (getTreeViewer() != null) {
			getTreeViewer().setSelection(new StructuredSelection(region));
			getTreeViewer().reveal(region);
			getTreeViewer().expandToLevel(region, TreeViewer.ALL_LEVELS);
		}
			
	}
	catch (BadLocationException e) {		
		e.printStackTrace();
	}		
}
```

The listener has to be registered during creation of `MarkdownTextOutlinePage`:
```
// outline page will be notified about changes of the caret position
getSourceViewer().getTextWidget().addCaretListener(outlinePage);
```

|_Implementing only_ISelectionListener_in `MarkdownTextOutlinePage` is not enough!_|
|:---------------------------------------------------------------------------------|


#### Property sheet ####
To show information about the current selection, the `Property View` is provided. If we want to show a property of the node (`ITypedRegion`) selected in the content provider, the selection of the underlying tree viewer has to be propagated.
It means that the selection provider has to be configured in the method `createControl` of the content outline page (it registers the tree viewer as a selection producer in `ISelectionService`):
```
getSite().setSelectionProvider(treeViewer);
```

The _Property view_ shows an object implementing the _IPropertySource_ interface (provider information - key-value pairs - about the selected object).
Similarly to previous examples, the selected node (of the type `ITypedRegion) has to be adapted to `IPropertySource`.

There are two ways:
  1. changing implementation of `ITypedRegion` so that it implements the `IAdaptable` interface.
  1. providing adapter factory

However, the first approach is not possible because implementation of `ITypedRegion` is a part of Eclipse base plugins.
Hence, it is necessary to register an adapter factory for the given type:
  * open plugin.xml
  * add a new extension org.eclipse.core.runtime.adapters
    * add a new factory:
      * _adaptableType_ = `ITypedRegion` (what is adapted)
      * _class_ = `cz.cuni.mff.d3s.nprg044.twitter.editor.adapters.EditorsAdapterFactory.java` (adaptation factory)
      * add a new sub-element adapter (what is a target of adaptation)
        * _type_ = `org.eclipse.ui.views.properties.IPropertySource`

Implement the factory:
```
public class EditorsAdapterFactory implements IAdapterFactory {

	private static final Class[] SUPPORTED_ADAPTERS = { IPropertySource.class };

	// return adaptation of a TypedRegion object to the IPropertySource interface
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if  (IPropertySource.class.equals(adapterType)) {
			return new TypedRegionPropertySource((ITypedRegion) adaptableObject);
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return SUPPORTED_ADAPTERS;
	}
}
```

and the associated implementation of the `IPropertySource` interface:
```
public class TypedRegionPropertySource implements IPropertySource {

	private static final String TYPED_REGION_VALUE_ID = "typedregion.value";
	private static final String TYPED_REGION_OFFSET_ID = "typed.region.offset";
	private static final String TYPED_REGION_LENGTH_ID = "typed.region.length";
    
	private ITypedRegion typedRegion;
	private IPropertyDescriptor[] propertyDescriptors;
	
	public TypedRegionPropertySource(ITypedRegion adaptableObject) {
		this.typedRegion = adaptableObject;
	}
	
	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			// define supported properties
			IPropertyDescriptor value = new PropertyDescriptor(TYPED_REGION_VALUE_ID, "Region value");
			IPropertyDescriptor offset = new PropertyDescriptor(TYPED_REGION_OFFSET_ID, "Region offset");
			IPropertyDescriptor length = new PropertyDescriptor(TYPED_REGION_LENGTH_ID, "Region length");
			
			propertyDescriptors = new IPropertyDescriptor[] {value, offset, length};
		}
		
		return propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (TYPED_REGION_VALUE_ID.equals(id)) {
			return "N/A";
		}
		else if (TYPED_REGION_OFFSET_ID.equals(id)) {
			return typedRegion.getOffset();
		}
		else if (TYPED_REGION_LENGTH_ID.equals(id)) {
			return typedRegion.getLength();
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}
	
	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
	}
}
```


## Recommended Reading & Links ##
  * Lars Vogella, Eclipse Editors Tutorial - http://www.vogella.de/articles/EclipseEditors/article.html
  * Lars Vogella, Eclipse Forms API Tutorial - http://www.vogella.de/articles/EclipseForms/article.html
  * Eclipse RCP How-to - http://wiki.eclipse.org/Eclipse_RCP_How-to
  * Eclipse FAQ
    * http://wiki.eclipse.org/FAQ_What_is_a_document_partition%3F
    * http://wiki.eclipse.org/FAQ_How_do_I_use_a_model_reconciler%3F
    * http://wiki.eclipse.org/FAQ_How_do_I_insert_text_in_the_active_text_editor%3F
  * Eclipse Article, Folding in Eclipse Text Editors - http://www.eclipse.org/articles/Article-Folding-in-Eclipse-Text-Editors/folding.html
  * Eclipse Article, Adapters - http://www.eclipse.org/articles/article.php?file=Article-Adapters/index.html