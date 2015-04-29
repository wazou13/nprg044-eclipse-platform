# Eclipse UI basics and views #


# SWT and JFace #
## SWT ##
SWT is a widget toolkit that provides API built on top of a native OS GUI platform.
It is a part of the Eclipse platform, but you can use it in standalone applications.
It is event-driven. The events are handled by the UI thread (the thread which creates the `Display` object).

Applications that wish to call UI code from a non-UI thread must provide a `Runnable` that calls the UI code.
The methods `syncExec(Runnable)` and `asyncExec(Runnable)` in the Display class are used to execute these runnables in the UI thread during the event loop.
See http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/guide/swt_threading.htm.

  * Class `Display` - represents the connection between SWT and the underlying platform's GUI system. Displays are primarily used to manage the platform event loop and to control the communication between the UI thread and other threads. You must create a display before creating any windows, and you must dispose the display when your shell is closed. This is typically done by the Eclipse workbench.

  * Class `Shell` is a _window_ managed by the OS platform window manager. Top level shells are those that are created as a child of the display. These windows are the windows that users can move, resize, minimize, and maximize while using the application. Secondary shells are those that are created as a child of another shell. These windows are typically used as dialog windows or other transient windows that only exist in the context of another window (so called _composite widgets_).

  * Class `SWT` - contains definition of style bits. E.g., SWT.YES, SWT.NO, SWT.VERTICAL, SWT.BORDER. Interpretation of the style bits depends on a particular widget (e.g., SWT.SINGLE - single selection, single line).

Typical structure of standalone SWT application implementation:
```
  public static void main (String [] args) {
      // create the top-level window	  
      Display display = new Display();
      Shell shell = new Shell(display);

      Label label = new Label(shell, SWT.CENTER);
      label.setText("Hello_world");

      // cover the whole area of the shell
      label.setBounds(shell.getClientArea());

      /* Your task: use more widgets here */

      shell.open();

      // main event loop	 
      while (!shell.isDisposed()) {
         if (!display.readAndDispatch()) display.sleep();
      }

      display.dispose();
   }
```

![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/swt-example00.png](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/swt-example00.png)

Observations:
  * A new widget always requires its parent.
  * An object of the class `Widget` represents a handle to the OS resource. Needs to be disposed explicitly!

|Note: _SWT requires the user to explicitly free any OS resources which were allocated. In SWT, the `Widget.dispose()` method is used to free resources associated with a widget._|
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

There are several simple rules for disposing widgets:
  * If the user creates a widget using its constructor, he/she also has to dispose it.
  * When a `Composite` widget is disposed, also its children are recursively disposed.
  * If the user gets a widget without calling its constructor, he/she cannot dispose it.
  * If the user passes a reference to some widget, he/she cannot dispose it while the widget is still in use.

There are three basic types of widgets - _Control, Layout, Event_.

### SWT Controls ###
The package `org.eclipse.swt.widgets` provides the widgets: `Label`, `Tree`, `Table`, `Text`, `List`, `Button`, `Canvas`, and many others
- see [Eclipse Help SWT](http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/swt.htm) for the full list.
You can fully control these widgets - in particular, you can configure fonts, colors, dimension, and borders.

### SWT Layouts ###
The package `org.eclipse.swt.layout` provides:
  * `FillLayout`
    * Lays out controls in a single row or column, forcing them to be of the same size.
  * `GridLayout`
    * Positions the children widgets by rows and columns.
  * `RowLayout`
    * Places the children either in horizontal rows or vertical columns.
  * `FormLayout`
    * Positions the children by using FormAttachments to optionally configure the left, top, right and bottom edges of each child.


### SWT Events ###
SWT is event driven. The package `org.eclipse.swt.events` provides various kinds of events - modified text, key pressed, mouse moved, drag, and so on (see http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/swt.htm).
The `Control` class has methods for registering listeners to various kinds of events.

### SWT graphics ###
Class `GC` (graphical context) can be used to draw customized widgets. See http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/guide/swt_graphics.htm.

### Example of SWT ###

  * create a new plug-in project `swt.examples`
  * add the `org.eclipse.ui` plug-in into dependencies
  * create a new class `swt.examples.Example01` with the main method
```
// based on SWT code snippets http://www.eclipse.org/swt/snippets/
public class Example01 {	
	public static void main(String[] args) {
		// create display
		Display display = new Display();
		// top-level widget is a shell
		final Shell shell = new Shell(display);

		shell.setLayout(new GridLayout());
		final Composite c = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		c.setLayout(layout);

		for (int i = 0; i < 5; i++) {
			Button b = new Button(c, SWT.PUSH | SWT.FLAT);
			b.setText("Button " + i);
		}

		Button b = new Button(shell, SWT.PUSH);
		b.setText("add a new button at row 2 column 1");
		final int[] index = new int[1];
		b.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Button s = new Button(c, SWT.PUSH);
				s.setText("By click " + index[0]);
				index[0]++;
				Control[] children = c.getChildren();
				s.moveAbove(children[3]);
				// recompute layout of widgets containing s
				shell.layout(new Control[]{ s });
			}
		});

		shell.open();
		shell.pack();

		// main event loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}

		// dispose widgets
		display.dispose();
	}
} 
```
  * run the class `swt.examples.Example01` as a _Java Application_ !

![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/swt-example01.png](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/swt-example01.png)

> |_Note: for more SWT code snippets please visit http://www.eclipse.org/swt/snippets/.__|
|:|

#### Form layout example ####
See [swt.examples.Example02](http://code.google.com/a/eclipselabs.org/p/nprg044-eclipse-platform/source/browse/trunk/2013-14/03-ui-basics-views/swt.examples/src/swt/examples/Example02.java).

![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/swt-form-layout-example.png](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/swt-form-layout-example.png)

## JFace ##
The packages `org.eclipse.jface.*` provide implementation of the JFace UI toolkit on top of SWT. Its purpose is to simplify the implementation of UI.
It handles:
  * viewers
  * actions
  * images and fonts registers
  * dialogs and wizards

Notes:
  * The method `getControl()` of JFace widgets returns the associated SWT control.
  * JFace also provides a way to automatically bind widget with data.

### Viewers ###
JFace widgets which display structured data - list, tree, tables (encapsulates native SWT widgets) - `ListViewer`, `TreeViewer` and `TableViewer`. They support also data sorting, filtering, selection, and visualization.

The basic concepts for viewers are the following:
  * _input element_  - the main object that the viewer is displaying (editing). It can be anything (table, JSON object, file, URI).
  * _content provider_ - an object that returns data which will be shown by the viewer. The provider takes the input element and returns an array of data based on the content of the input element. See the interfaces `IContentProvider` and `IStructuredContentProvider` which are used to obtain structured data. They can be also lazy in case when the viewer specifies SWT.VIRTUAL flag. Then the content provider has to implement the `ILazyContentProvider` interface and produce data on demand.
  * _label provider_ - defines how the data returned by the content provider are shown (image, text in colors). See the interface `ILabelProvider`.
    * if the label provider also implements a dedicated interface (e.g., `IColorProvider`,`IFontProvider`, `ILabelDecorator`) it can be handled by the viewer in a specific way.

The shared implementation is provided by the `ContentViewer` class:
  * method `void setInput(Object o)`
  * method `void setContentProvider(IContentProvider contentProvider)`
  * method `void setLabelProvider(IBaseLabelProvider labelProvider)`

> |Note: _Instances of the particular content and label providers are not intended to be shared across multiple viewers. Even if all your viewers use the same type of a content or label provider, each viewer should be initialized with its own instance of the provider class. The provider life cycle protocol is designed for a 1-to-1 relationship between a provider and its viewer._|
|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

#### Table viewer example ####
|_Goal: create a simple application showing a list of followers for a given twitter user._|
|:----------------------------------------------------------------------------------------|

See [jface.examples.Example01](http://code.google.com/a/eclipselabs.org/p/nprg044-eclipse-platform/source/browse/trunk/2013-14/03-ui-basics-views/jface.examples/src/jface/examples/Example01.java).

![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/jface-table-viewer-example.png](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/jface-table-viewer-example.png)

# Workbench UI #
Eclipse workbench provides a GUI model which is not connected to JFace. It provides workbench parts represented by _views_ and _editors_.
```
IAdaptable
 |- IWorkbenchPart
   |- IViewPart 
   |- IEditorPart
```

The class `IWorkbenchPart`:
  * method `void createPartControl(Composite parent)`
  * method `IWorkbenchPartSite getSite()` - _site_ is a primary interface between a workbench part and the workbench. For example, it is used for registering menus and global actions.
  * method `String getTitle()`
  * method `Image getTitleImage()`
  * method `void setFocus()` - asks this part to take focus within the workbench. The part must assign focus to one of the controls contained in the part's parent composite. It is called by the workbench.

> |Note: _There are also interfaces `IWorkbenchPart2` and `IWorkbenchPart3`. They represent new versions of `IWorkbenchPart`. This is a common concept of introducing new version of interfaces in Eclipse._|
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

The class `IViewPart`:
  * `void init(IViewSite site)` - initializes this view with the given view site. It is called by the workbench automatically during initialization of the part.

The class `IEditorPart`:
  * `void init(IEditorSite site, IEditorInput input)` - initializes this editor with the given editor site and editor input (e.g., a file or a URI). It is automatically called by the workbench.

## Views ##
A view is class implementing the `IViewPart` interface. It shows data with the help of some [viewer](Tutorial03UIbasics#Viewers.md). It is beneficial to start with extending the `ViewPart` class that implements basic infrastructure.

Important points:
  * method `createControlPart()` - it creates a graphical viewer shown by the view. For example, a table, a tree, or a hand-made graph visualizator. Any SWT code can be here.
    * calling method `viewer.setInput()` - set the viewer input
    * calling method `viewer.setContentProvider()`
    * calling method `viewer.setLabelProvider()`
    * registering action hooks/menus - e.g., via calling `getSite().registerContextMenu(menuMgr, viewer);`

### Example: `cz.cuni.mff.d3s.nprg044.twitter.ui.view` ###
> | Note: _Plug-in name follows the common Eclipse naming convention - plug-in contributes into the UI by providing new views hence its name contains the `ui.view` suffix._|
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

> Goal: _Create a plugin that provides a view showing the message timeline for a given Twitter user._

  * create a new plug-in `cz.cuni.mff.d3s.nprg044.twitter.ui.view`
  * configure `plugin.xml`:
    * add a dependency on the plug-in `cz.cuni.mff.d3s.nprg044.twitter.api`
    * add dependencies on the plug-ins `org.eclipse.ui` and `org.eclipse.core.runtime`
    * define extension points:
      * `org.eclipse.ui.views`:
        * add a new category _Twitter Category_
          * category will be shown in the menu _Window > Show Views > Others..._
        * add a new view
          * define ID, name, category (set to Twitter category), icon
            * fastViewWidthRatio = 0.3 (one third of the workbench width)
            * allowMultiple = true (we will allow showing timelines of various users)
          * set the `class` attribute to `cz.cuni.mff.d3s.nprg044.twitter.ui.view.TwitterMessageTimelineView`
    * implements the class (inherits from the `ViewPart` class)
      * method `createPartControl(Composite parent)
        * create `GridLayout`, pass it to `parent.setLayout()`
```
// grid with one column
GridLayout layout = new GridLayout(1, true);
parent.setLayout(layout);
```
        * create a `Text` control `searchBox` with the flags `SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH`
          * setup the layout data - `new GridData(GridData.FILL_HORIZONTAL)`
```
// it allows single line and supports searching
searchBox = new Text(parent, SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH);		
// modify layout: fill the available horizontal space
searchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
searchBox.setText("vtipy");
```
        * create a `TableViewer` control `viewer` with the flags `SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER`
          * setup the layout data `new GridData(GridData.FILL_BOTH)`
            * you have to call `viewer.getControl()` to access the underlying SWT control (`TableViewer` is a JFace widget)
```
// it has a border and scroll bars 
viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
// fill the available horizontal and vertical space
viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
```
          * set the input to `viewer.setInput(getViewSite())`
      * if you need lazy content resolving then put `SWT.VIRTUAL` into the `viewer`'s flags. But, the content provider has to implement `ILazyContentProvider` instead of `IStructureContentProvider` in that case
```
public class TwitterMessageTimelineView extends ViewPart {
	private static final String[] COLUMN_NAMES = {"#", "username", "message"};
	private static final int[] COLUMN_WIDTHS = {30, 100, 200};
	private Text searchBox;
	private TableViewer viewer;

	public TwitterMessageTimelineView() {	
	}

	/**
	 * This method creates a graphical representation of this view.
	 * Any widgets (SWT) can be used here.
	 */
	@Override
	public void createPartControl(Composite parent) {
		// grid with one column
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		
		// create text control 'searchbox'
		// it allows single line and supports searching
		searchBox = new Text(parent, SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH);		
		// modify layout: fill the available horizontal space
		searchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		searchBox.setText("vtipy");
		
		// create a table viewer control (JFace)
		// it has a border and scroll bars 
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	
		// add columns into the table
		createColumns(viewer);
	
		// set provider of data in the columns
		viewer.setContentProvider(new MessageTimelineContentProvider(progressBar));
		
		// set provider of the column labels
		viewer.setLabelProvider(new MessageTimelineLabelProvider());

		// set input of the content provider
		// viewer should generate the table based on the content of the search box
		viewer.setInput(getViewSite());
		
		// getControl() returns the underlying SWT widget
		// fill the available horizontal and vertical space
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	
		// define style of the underlying table		
		viewer.getTable().setLinesVisible(true);		
		viewer.getTable().setHeaderVisible(true);

		// make selection in the table available to other controls
		getSite().setSelectionProvider(viewer);
	}

	private void createColumns(TableViewer tableViewer) {
		for (int i = 0; i < COLUMN_NAMES.length; i++) {
			TableViewerColumn tvColumn = new TableViewerColumn(tableViewer, SWT.NULL);
			TableColumn column = tvColumn.getColumn();
			column.setWidth(COLUMN_WIDTHS[i]);
			column.setText(COLUMN_NAMES[i]);
			
			// NOTE
			// it is also possible to register separated cell providers 
			// using the method TableViewerColumn.setLabelProvider
			// see CellLabelProvider or StyledCellLabelProvider
		}		
	}

	/**
	 *  This part has the focus now (in the workbench).
	 *  It must assign focus to one control inside it.
	 */
	@Override
	public void setFocus() {
		this.searchBox.setFocus();
	}
}
```

  * implement a content provider `MessageTimelineContentProvider` inheriting from `IStructureContentProvider`
    * implement the `inputChange` method
      * register/deregister listeners to the input, refreshing of the viewer is not necessary because it is done by the underlying infrastructure
```
/**
 * Notification that a different control is now the input for this provider.
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	this.viewer = viewer;
		
	if (oldInput == newInput) return;
		
	// remove listener for the old input of a control type
	if (oldInput instanceof Control) {
		Control c = (Control) oldInput;
		if (!c.isDisposed()) {
			((Control) oldInput).removeKeyListener(keyListener);
		}
	}
		
	// we must now listen for keys on a different control/widget
	// register listener for the new input of a control type
	if (newInput instanceof Control) {
		Control c = (Control) newInput;
		if (!c.isDisposed()) {
			((Control) newInput).addKeyListener(keyListener);
		}
	}
}
```
    * add a key listener
```
private KeyListener keyListener = new KeyAdapter() {
	private String username;
		
	public void keyReleased(KeyEvent e) {
		// watch for "Enter" keys
		if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
			// but only for text input widgets (some other widgets can be there)
			if (e.widget instanceof Text) {
				String newUsername = ((Text) e.widget).getText();
				if (!newUsername.equals(username)) {
					username = newUsername;
					// run in the UI thread
					e.display.asyncExec(new Runnable() {
						@Override
						public void run() {
							// it is necessary to check that the widget is not disposed
							if (!viewer.getControl().isDisposed()) {
								viewer.refresh();
							}
						}
					});
				}
			}
		}
	};
};
```
    * implement the `getElements` method
      * parameter `inputElement` is an input element set by calling the `setInput` method of the viewer
```
public Object[] getElements(Object inputElement) {
	String username = getUsername(inputElement);
	if (username == null || username.equals("")) {
		return EMPTY_CONTENT;
	}
		
	Twitter twitter = TwitterAuthUtil.getTwitterInstance();
		
	// create a list of user statuses to be displayed in the viewer (table)
	try {
		List<Status> statuses = new ArrayList<Status>();
			
		User user = twitter.showUser(username);
		if (user != null) {
			statuses.add(twitter.showStatus(user.getStatus().getId()));
		}
			
		if (!statuses.isEmpty()) {
			return statuses.toArray();
		}
		else {
			return EMPTY_CONTENT;
		}
	}
	catch (Exception e) {
		return new String[] {e.getMessage()};
	}
}

private String getUsername(Object inputElement) {
	if (inputElement instanceof Text) {
		return ((Text) inputElement).getText();
	}
	return null;
}
```
    * in the method `createPartControl` of the class `TwitterMessageTimelineView`, use the call `viewer.setInput(searchBox)` instead of the original variant
      * searchBox will serve as an input for the content provider
      * It can be changed. The change invokes the method `inputChanged` of the content provider.

  * implement the `MessageTimelineLabelProvider` by extending the class `LabelProvider` (that implements `ILabelProvider`) and implementing `ITableLabelProvider`.
    * it is neccessary to return the right text/image for each column
      * there is also another way via the method `TableViewerColumn.setLabelProvider()` to configure separated label provider for each column
    * for images you can use built-in images accessible via the call `PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);`
```
public class MessageTimelineLabelProvider extends LabelProvider implements ITableLabelProvider {

	// return image (a part of the label) for the given column
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Status) {
			Status status = (Status) element;
			
			// we want to show image only in the first column
			switch(columnIndex) {
				case 0:
					return getImage(status);
				default:
					return null;
			}
		}
		
		if (element instanceof String && columnIndex == 0) {
			// get some built-in warning image
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		}
		
		return null;
	}

	// return text part of the label for the given column
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Status) {
			Status status = (Status) element;
			
			// show user name in the second column and message text (status) in the third column
			switch (columnIndex) {
				case 0:
					return null;
				case 1:				
					return '@' + status.getUser().getScreenName();
				case 2:
					return status.getText();
				default:
					return null;
			}			
		}
	
		return null;
	}
	
	@Override
	public Image getImage(Object element) {		
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
```

## Your tasks ##
  * create a new view providing a list of usernames
  * publish a viewer as a selection source

# Links #
  * Eclipse help: JFace - http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/jface.htm
  * Eclipse help: SWT - http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/swt.htm
  * SWT: Standard Widget Toolkit - http://www.eclipse.org/resources/resource.php?id=241
  * SWT code snippets - http://www.eclipse.org/swt/snippets/
  * JFace code snippets - http://wiki.eclipse.org/index.php/JFaceSnippets
  * Lars Vogel: JFace tutorial - http://www.vogella.de/articles/EclipseJFaceTable/article.html
  * Eclipse tutorial: Creating Your Own Widgets using SWT - http://www.eclipse.org/articles/Article-Writing%20Your%20Own%20Widget/Writing%20Your%20Own%20Widget.htm
  * Eclipse tutorial: Understanding Layouts in SWT - http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html
  * Eclipse help: Core expressions - http://wiki.eclipse.org/Command_Core_Expressions