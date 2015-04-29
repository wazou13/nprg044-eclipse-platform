# Advanced UI, perspectives, wizards #


# Workbench UI #
Additional concepts supported by the Eclipse workbench GUI include the following: actions, menus, toolbars, property sheets.

## Actions in Views ##
### Context and Expressions ###
We want to add the delete command that will clear the timeline view.

First we have to define the context in which the delete command will be applied. For this purpose, expressions can be used. Each expression defines a condition which has to be satisfied. The expression can be re-used in other definitions.
  * open `plugin.xml`
    * add a new dependency `org.eclipse.core.expressions`
  * add a new extension `org.eclipse.core.expressions.definitions`
    * define ID `cz.cuni.mff.d3s.nprg044.twitter.ui.view.inTimelineView`
      * add `with` sub-element with the value `activePartId` (for more variables see [Core Expressions Help](http://wiki.eclipse.org/Command_Core_Expressions))
        * add `equals` sub-element with the value `cz.cuni.mff.d3s.nprg044.twitter.ui.view.MessageTimelineView` (it is the ID of the previously defined MessageTimelineView)

Add a new handler for the global delete command (with the ID `org.eclipse.ui.edit.delete`) that will be active if and only if the timeline view has the focus.
  * open `plugin.xml`
  * add a new extension `org.eclipse.ui.handlers`
    * add a new handler:
      * setup _commandId_ to `org.eclipse.ui.edit.delete`
      * add a new sub-element `activeWhen`
        * add a new `reference` sub-element and set it to `cz.cuni.mff.d3s.nprg044.twitter.ui.view.inTimelineView`
    * add the handler implementation:
      * look at the helper class `HandlerUtil`
```
public class CleanTimelineView extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the active workbench part when the event occurred
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		// this handler for the delete command applies only to message timeline view
		if (part instanceof TwitterMessageTimelineView) {
			((TwitterMessageTimelineView) part).cleanTimeline();
		}
		return null;
	}
}
```
  * add the new method `cleanTimeline` into the class `TwitterMessageTimelineView`:
```
public void cleanTimeline() {
	searchBox.setText("");
	viewer.refresh();
}
```
  * try to run the example and observe behavior of the delete command

### Toolbar ###
Let's put the delete command into a toolbar of the `TwitterMessageTimelineView` view:
  * open `plugin.xml`
  * add the extension `org.eclipse.ui.menus`
    * add a new _menuContribution_
      * set _locationURI_ to `toolbar:cz.cuni.mff.d3s.nprg044.twitter.ui.view.MessageTimelineView` (toolbar:<view ID>)
      * add a sub-element _command_ referring to the `org.eclipse.ui.edit.delete` command

### Menu ###
  * as above, but the _locatioURI_ is `menu:cz.cuni.mff.d3s.nprg044.twitter.ui.view.MessageTimelineView` (menu:<view ID>)

### Context menu ###
The context menu has to be registered first to allow contribution via the extension point 'org.eclipse.ui.menus'. The view can have multiple context menus registered. Default ID of the context menu is the part ID.

  * Add the new method `createContextMenu` into the class `TwitterMessageTimelineView`
    * The method is called from the `createPartControl` method
```
private void createContextMenu() {
	MenuManager menuManager = new MenuManager("#PopupMenu");
	// remove old items from the menu every time just before it is
	// displayed again (possibly for a different table element)
	menuManager.setRemoveAllWhenShown(true);
	menuManager.addMenuListener(new IMenuListener() {
		@Override
		public void menuAboutToShow(IMenuManager manager) {
			// add separator just before other contributed items (set via extensions)
			fillContextMenu(manager);
		}
	});
	// create the actual context menu for the table viewer
	Menu menu = menuManager.createContextMenu(viewer.getControl());
	viewer.getControl().setMenu(menu);
	getSite().registerContextMenu(menuManager, viewer);
}
```
  * add the method `fillContextMenu`
    * it is necessary to add a separator for additions - it is the location where new actions will be placed.
```
private void fillContextMenu(IMenuManager manager) {
	manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));				
}
```

  * add a _menuContribution_ like in the cases above, but set the _locationURI_ to `popup:cz.cuni.mff.d3s.nprg044.twitter.ui.view.MessageTimelineView` (for one context menu the locationURI has the form `popup:<view ID>`)

  * add a new _command_ definition `cz.cuni.mff.d3s.nprg044.twitter.ui.view.showMessageDetails` with the default command handler which will show a _Property view_:
```
public class ShowMessageDetails extends AbstractHandler {
	// response for a particular selection in the context menu
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// show the properties dialog
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView("org.eclipse.ui.views.PropertySheet");
		} catch (PartInitException e) {
			throw new ExecutionException(e.getMessage(), e);
		}	
		return null;
	}
}
```
    * add the command into the context menu


## Property sheet ##
We want to show the message details in a _Property View_. The message is represented by the object `Status`.

At first we have to set the viewer to provide a current selection. It is necessary to have the line
```
// make selection in the table available to other controls
getSite().setSelectionProvider(viewer);
```
in the method `createPartControl(Composite parent)` of the class `TwitterMessageTimelineView`.

Then we have to adapt `Status` to the `IPropertySource` type which provides details about the status (plugin `org.eclipse.ui.views` has to be in the plug-in dependencies). There are two possible ways of adaptation:
  1. the `Status` object implements the `IAdaptable` interface:
```
public Object getAdapter(Class adapter) {
  return IPropertySource.class.equals(adapter) ? return new TwitterStatusPropertySource(this) : null;
}
```
    * this is a common solution if you can change the source code of the `Status` class. However, in our case it is provided by a third party library. But, there is another solution:
  1. register an adapter factory for the given type:
    * open `plugin.xml`
    * add a new extension `org.eclipse.core.runtime.adapters`
      * add a new factory:
        * _adaptableType_ = `twitter4j.Status` (what is adapted)
        * _class_ = `cz.cuni.mff.d3s.nprg044.twitter.ui.view.adapters.TwitterAdapterFactory` (adaptation factory)
        * add a new sub-element `adapter` (what is a target of adaptation)
          * _type_ = `org.eclipse.ui.views.properties.IPropertySource`

  * implement the factory class `cz.cuni.mff.d3s.nprg044.twitter.ui.view.adapters.TwitterAdapterFactory`:
```
public class TwitterAdapterFactory implements IAdapterFactory {

	private static final Class[] SUPPORTED_ADAPTERS = { IPropertySource.class };

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if  (IPropertySource.class.equals(adapterType)) {
			return new TwitterStatusPropertySource((Status) adaptableObject);
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return SUPPORTED_ADAPTERS;
	}
}
```

  * and implement the class `TwitterStatusPropertySource`
```
public class TwitterStatusPropertySource implements IPropertySource {
	private static final String MSG_ID = "twitter.prop.msg";
	private static final String RETWEET_COUNT_ID = "twitter.prop.retweet.count";
	
	private Status status;	
	private IPropertyDescriptor[] propertyDescriptors;

	public TwitterStatusPropertySource(Status status) {
		this.status = status;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			// define supported properties
			IPropertyDescriptor descMessage = new PropertyDescriptor(MSG_ID, "Message");
			IPropertyDescriptor retweetCount= new PropertyDescriptor(RETWEET_COUNT_ID, "Retweet count");
			propertyDescriptors = new IPropertyDescriptor[] { descMessage, retweetCount };
		}
		
		return propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(MSG_ID)) {
			return status.getText();
		} else if (id.equals(RETWEET_COUNT_ID)) {
			return status.getRetweetCount();
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

| _The concept of adaptors is one of the most important concepts in the whole Eclipse platform. It allows to use one object in different contexts._ |
|:--------------------------------------------------------------------------------------------------------------------------------------------------|

## Selection service ##
In the previous example, we saw that the viewer was registered as a selection provider (see the line `getSite().setSelectionProvider(viewer);` in the class `TwitterMessageTimelineView`). There is also a way to listen to selection events via registering an implementation of `ISelectionListener` with the help of a selection service (e.g., `getSite().getWorkbenchWindow().getSelectionService()`).

## Your tasks ##
  * register a listener to the new view via the _Selection service_
    * listener has to look at the current selection and repaint the viewer

# Perspective #
To setup a new perspective it is necessary to define the extension `org.eclipse.ui.perspectives` and provide an implementation of the `IPerspectiveFactory` interface.
The implementation creates the initial layout of the perspective (views, editor areas, menus) via `IPageLayout`.
```
public class TwitterPerspectiveFactory implements IPerspectiveFactory {
	
	/** The ID of this GUI element. */
	public static final String PERSPECTIVE_ID = "cz.cuni.mff.d3s.nprg044.twitter.ui.perspective.TwitterPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		// define layout of the perspective
		addViews(layout);
		addActionSets(layout);
		addNewWizardShortcuts(layout);
		addPerspectiveShortcuts(layout);
		addViewShortcuts(layout);
	}

	private void addViews(IPageLayout layout) {
		// create the left panel view area programmatically
		// its position is left with respect to editor area
		// left panel will occupy 25% of the horizontal space originally given to the editor area
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, layout.getEditorArea());
		
		// left panel contains the message timeline and properties view/sheet
		left.addView(TwitterMessageTimelineView.ID);
		left.addView(IPageLayout.ID_PROP_SHEET);
		
		// create the bottom panel view area
		// bottom panel will occupy 25% of the vertical space originally given to the editor area
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, layout.getEditorArea());
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView("org.eclipse.pde.runtime.LogView");
		
		// create the right panel view area
		// right panel will occupy 25% of the horizontal space originally given to the editor area
		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, 0.75f, layout.getEditorArea());
		right.addView(IPageLayout.ID_OUTLINE);
	}

	private void addActionSets(IPageLayout layout) {
	}	

	private void addPerspectiveShortcuts(IPageLayout layout) {
		// add button that will turn of this perspective into the top right corner
		layout.addPerspectiveShortcut(PERSPECTIVE_ID);
	}

	// wizards like those in "File -> New"
	private void addNewWizardShortcuts(IPageLayout layout) {
		//layout.addNewWizardShortcut(LoginWizard.ID);
	}

	// shortcuts like those in "Window -> Show View"
	private void addViewShortcuts(IPageLayout layout) {
	}
}
```

## Perspective extension ##
It is possible to modify existing perspectives via the extension point `org.eclipse.ui.perspectiveExtensions`
  * define _targetID_
    * regexp or ID of the target perspective (e.g,. `cz.cuni.mff.d3s.nprg044.twitter.ui.perspective.TwitterPerspective`)
  * define target perspective modification:
    * add new wizard shortcuts
    * add perspective shortcuts
    * add view shortcuts
    * hide items

|_To reflect the changes it is often necessary to reset the perspective._|
|:-----------------------------------------------------------------------|


# Wizards #
A wizard is a dialog implementing the `IWizard` interface. It can be defined via extensions `org.eclipse.ui.newWizards`, `org.eclipse.ui.importWizards` or `org.eclipse.ui.exportWizards`, or implemented ad-hoc.

  * create the wizard for a new message
    * define the extension `org.eclipse.ui.newWizards`
    * add a new category `Twitter` (it will be shown in the dialog for wizard selection)
    * define _name_,
    * define _finalPerspective_ as _Twitter perspective_
    * implement the _class_ attribute
      * the wizard has to implement the `IWizard` interface; however, it is beneficial to extend the `Wizard` class
```
public class NewMessageWizard extends Wizard implements INewWizard {
	
	private MessageWizardPage messagePage;

	public NewMessageWizard() {
		setWindowTitle("Post a new message");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	// create our wizard page in a lazy manner
	@Override
	public void addPages() {
		messagePage = new MessageWizardPage("Message");
		addPage(messagePage);
	}

	// perform response to the user clicking on the "Finish" button
	@Override
	public boolean performFinish() {
		if (messagePage.canFlipToNextPage()) {
			System.out.println("Message posted: " + messagePage.getMessageText());			
			return true;
		} 
		else {
			return false;
		}
	}
}
```
    * the wizard has to setup pages in the method `addPages` and perform the finish action in the method `performFinish`

The wizard page represents one page shown in the wizard. It has to setup its layout and validation of the user input. The wizard page is represented by the interface `IWizardPage`, but it is better to extend the class `WizardPage`.
```
public class MessageWizardPage extends WizardPage {

	public static final int MAX_CHAR = 150;
	
	private TextViewer messageEditor;
	private Label characterCount;

	protected MessageWizardPage(String pageName) {
		super(pageName);
		
		setTitle("Post a new message");
		
		setDescription("Post twitter message. It should not contain more than 150 characters!");
		
		setPageComplete(false);
	}

	// create layout of the wizard page
	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		setControl(composite);
		
		messageEditor = new TextViewer(composite, SWT.BORDER | SWT.MULTI);
		messageEditor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
		messageEditor.getTextWidget().addModifyListener(new ModifyListener() {
			// check the number of characters after each modification
			@Override
			public void modifyText(ModifyEvent e) {
				updateCharactersCount(messageEditor.getTextWidget().getText().length());				
			}
		});
		
		characterCount = new Label(composite, SWT.NONE);
		characterCount.setText(MAX_CHAR + " characters");				
		characterCount.setLayoutData(new GridData(SWT.RIGHT, SWT.BEGINNING, false, false));		
	}

	protected void updateCharactersCount(int length) {		
		characterCount.setText((MAX_CHAR-length) + " characters");
		
		if (validatePage()) {
			setPageComplete(true);				
		} 
		else {
			setPageComplete(false);
		}
	}
	
	// validates the user input
	protected boolean validatePage() {
		int length = messageEditor.getTextWidget().getText() != null ? messageEditor.getTextWidget().getText().length() : 0;
	
		if (length > 0 && length <= MAX_CHAR) {
			setErrorMessage(null);
			return true;
		} 
		else {
			setErrorMessage("Character count has to be in range (0..150>" );
			return false;
		}
	}
	
	// returns true if this page is complete and we can move to the next one
	@Override
	public boolean canFlipToNextPage() {		
		return validatePage();
	}

	public String getMessageText() {		
		return messageEditor.getDocument().get();
	}
}
```

|Note: _To launch the wizard programmatically, it is necessary to create an instance of the wizard and pass it to the instance of the `WizardDialog` class, which is responsible for presentation of the wizard._|
|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

# Jobs #
The `Job` class allows execution of user code in a separate thread.

`Job` includes the following methods:
  * `run()` - user has to override the method to implement the logic of a given job
  * `schedule()` - schedule the job
  * `join()` - wait for the job to finish
  * `cancel()` - cancel the job, only setup the flag published via the method `isCanceled()`

```
if (!job.cancel())
      job.join();
```

Job result signaled via `IStatus` (`IStatus.OK`, `IStatus.ERROR`)

Job types:
  * user - modal dialog is shown, can be put into the background. Configured via the call `setUser(true)`.
  * system - no contribution into the UI. E.g., jobs which are NOT initiated by a user. Configured via the call `setSystem(true)` before job scheduling.

## Periodic jobs ##
The job implementation has to reschedule itself - i.e., it needs to call `schedule(num)`.

## Reporting progress ##
Progress monitor is represented by the `IProgressMonitor` interface:
  * `monitor.beginTask("Getting follows", numberOfFollows);` - start a monitor for given units of work
  * `monitor.worked(1)` - one unit of work processed
  * `monitor.done()` - computation is finished

# Resources #
  * Eclipse workbench: Using the Selection Service - http://www.eclipse.org/articles/Article-WorkbenchSelections/article.html
  * Eclipse tutorial: Take control over you properties - http://www.eclipse.org/articles/Article-Properties-View/properties-view.html
  * Eclipse tutorial: The Eclipse Tabbed Properties View - http://www.eclipse.org/articles/Article-Tabbed-Properties/tabbed_properties_view.html
  * Eclipse Help - Wizards - http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/guide/dialogs.htm
  * Eclipse Help - Perspective - http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/guide/workbench_perspectives.htm
  * Eclipse Article - JFace Wizards - http://www.eclipse.org/articles/article.php?file=Article-JFaceWizards/index.html
  * Eclipse Atticle - On the Job: The Eclipse Jobs API - http://www.eclipse.org/articles/Article-Concurrency/jobs-api.html