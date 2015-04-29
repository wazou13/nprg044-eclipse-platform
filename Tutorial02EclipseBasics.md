# Eclipse basics #


## Overall Plan ##
The overall plan of the lectures about Eclipse is to show basics of the Eclipse RCP development on a simple example of [Twitter client](ExampleTwitterClientDesign.md).
The tutorial and source code are published at http://code.google.com/a/eclipselabs.org/p/nprg044-eclipse-platform/.

This text serves as an outline for the lectures.

## Eclipse installation ##
Go to http://www.eclipse.org/downloads/ and download _Eclipse for RCP and RAP developers_.
Unpack the downloaded archive and run the command `eclipse`.

## Basic concepts of Eclipse Workbench ##

#### Eclipse platform design ####
Eclipse IDE is a Java application running on top of the extended OSGi Equinox runtime. It is composed of OSGi bundles called Eclipse plugins.
![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/eclipse-architecture.jpg](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/eclipse-architecture.jpg)

  * _Platform runtime_ - Defines the extension points and plug-in model. It dynamically discovers plug-ins and maintains information about the plug-ins and their extension points in a platform registry. Plug-ins are started when required according to the user operation of the platform. The runtime is implemented using the OSGi framework. The runtime kernel is constituted by the plugins `org.eclipse.core.runtime` and `org.eclipse.osgi`.
    * _Plugin_ - an extended OSGi bundle. Defines `MANIFEST.MF` (bundle metadata) and `plugin.xml` (plugin metadata).
  * _Workbench UI_ - View of the platform. Defines extension points to inject new views and editors.

#### Eclipse installation ####
Important files and directories
```
eclipse/
 |-configuration/
  * config.ini
 |-features/
 |-plugins/
 * artifacts.xml
 * eclipse
 * eclipse.ini
```

#### Eclipse launcher ####
Eclipse can be started using a binary launcher which detects the available Java platform and launches the Eclipse Java application:
Parameters of the Eclipse Java application are stored in the file `eclipse.ini`.
```
-startup
plugins/org.eclipse.equinox.launcher_1.3.0.v20120522-1813.jar
--launcher.library
plugins/org.eclipse.equinox.launcher.win32.win32.x86_64_1.1.200.v20120522-1813
-product
org.eclipse.epp.package.rcp.product
--launcher.defaultAction
openFile
--launcher.XXMaxPermSize
256M
-showsplash
org.eclipse.platform
--launcher.XXMaxPermSize
256m
--launcher.defaultAction
openFile
-vmargs
-Dosgi.requiredJavaVersion=1.5
-Dhelp.lucene.tokenizer=standard
-Xms40m
-Xmx512m
```

Other useful arguments are (see also [Running Eclipse help page](http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.platform.doc.user/tasks/running_eclipse.htm)):
  * `-consoleLog` : shows log messages from the Eclipse Java application
  * `-clean` : cleans cached data, bundles, services information

> |Note: _Sometimes it is recommended to increase the parameter XX:MaxPermSize._|
|:----------------------------------------------------------------------------|

> |Note: _Product defines an application to be run. The default Eclipse installation runs `org.eclipse.ui.ide.workbench`. However, it can be changed. See the OSGi specification (`org.osgi.service.application`), the Eclipse extension point [`org.eclipse.core.runtime.applications`](http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/reference/extension-points/org_eclipse_core_runtime_applications.html) and the product definition._|
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|


### Basic terminology of Eclipse workbench ###
#### Workbench ####
The active Eclipse window. It is also referred to as the _desktop development environment_. It contains a _workbench page_ composed of views and editors.

#### Perspective ####
Eclipse IDE provides multiple workbench configurations, called _perspectives_. A perspective defines the organization of the workbench - layout of _views_, _editors_, _menus_, toolbars and their items.

#### Page ####
Defines mechanism of grouping included parts - views and editors.

#### Menus and toolbars ####

#### Views ####
A view typically shows a structural information, properties, or additional information about the edited document.

#### Editors ####
An editor is used to edit, view, or browse a document (e.g., file, virtual node).

#### Workspace ####
It is a core working area (i.e., directory) where your instance of Eclipse is running. It can be changed. It contains a set of various projects.

#### Projects ####
Project is a directory containing the _.project_ file describing project's metadata (name, dependencies, builders). The project's directory can be a part of the workspace directory or just linked to a workspace (typically used when the project is a part of a SVN/HG repository).

![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/screens/eclipse-terminology.png](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/screens/eclipse-terminology.png)

#### Resources ####
A resource represents an entity inside the project - it can be mapped to some existing entity (e.g., a file, folder, or a server) or it can be virtual. The Eclipse platform introduces its own resource model and management.

### Eclipse Plug-in ###
An Eclipse plug-in is an OSGi bundle with additional meta-information stored in the file `plugin.xml` (e.g., extension point definitions). It also brings additional MANIFEST.MF headers (e.g., `Eclipse-BundleShape` for controlling if the plugin should be deployed as a jar file or unpacked).

#### Plug-in Project structure ####
```
simple-eclipse-plugin\
  |-src
  |-lib
  |-META-INF
    * MANIFEST.MF
  * plugin.xml
```

#### Extension point ####
It is a well-defined (via xsd) location in the Eclipse infrastructure which helps plug-ins to contribute into the UI or Eclipse platform.
For example, definition of new views, editors, actions, jobs, early startup initializers.


---


## Plug-in project from existing JAR: `cz.cuni.mff.d3s.nprg044.twitter.api` ##
> |Goal: _Create an Eclipse plug-in from an existing JAR file_|
|:----------------------------------------------------------|

Why?
If you need to bundle a 3rd party library as an OSGi/Eclipse bundle.

How?
  * unpack `twitter4j-4.0.1.zip`
    * can be downloaded from http://twitter4j.org/
  * create a new plug-in project
    * menu _New > Project > Plug-in from existing JAR_:
      1. select JAR `twitter4j-core-4.0.1.jar`
      1. fill the project name, plug-in name, plug-in ID (must be unique)
      1. uncheck the option 'Unzip JAR archives into project'
  * the wizard will create a new project and setup its MANIFEST.MF
  * revisit MANIFEST.MF
    * tab: hide internal/implementation packages (**internal**)
      * _good practice_ is to publish only API packages

> |Note: _If you change the project build-path or MANIFEST.MF, they can be resynchronized. In such a case, you can open the file MANIFEST.MF, and on the first tab click on 'Update build path...'_|
|:-----|


---


## Simple plug-in project: `cz.cuni.mff.d3s.nprg044.twitter.api.test` ##
> |Goal: _Create a simple plug-in with an activator and log some messages in the activator. Launch the plugin and try to debug it._|
|:-------------------------------------------------------------------------------------------------------------------------------|

  * Create a new plugin-project
    * menu _New > Project > Plug-in Project_:
      1. fill the project name: `cz.cuni.mff.d3s.nprg044.twitter.api.test`
      1. fill
        * plug-in ID (should be unique): `cz.cuni.mff.d3s.nprg044.twitter.api.test`,
        * plug-in name: `Twitter API test`
        * enable the _Generate an activator_ check box
        * rename the `Activator` class to `TwitterTestActivator`
          * Activator is a class that handles the plug-in life-cycle (start/stop)
            * it is created when the plug-in's class is required
            * it is possible to specify an exception using the `exclude/include` directive in `MANIFEST.MF`
              * see [Bundle Activation](http://wiki.eclipse.org/Lazy_Start_Bundles)
  * Edit MANIFEST.MF and add the plug-ins `cz.cuni.mff.d3s.nprg044.twitter.api` and `cz.cuni.mff.d3s.nprg044.twitter.auth` to Dependencies
    * Download the bundle `cz.cuni.mff.d3s.nprg044.twitter.auth` from the subversion repository and change the authentication credentials based on `twitter4j.properties`
  * Edit the code of `TwitterTestActivator` and put `System.out.println` calls into the constructor, start and stop methods.
    * |Hint: start typing `sys` and then press _Ctrl+space_, the template dialog will be shown and you can select the required option (e.g., `System.out.println`)|
|:----------------------------------------------------------------------------------------------------------------------------------------------------------|
  * Append the method `showUserStatus()`
```
protected void showUserStatus(String username) {
    try {
        Twitter twitter = TwitterAuthUtil.getTwitterInstance();		
        User user = twitter.showUser(username);
        if (null != user.getStatus()) {
            System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
        } else {
            // the user is protected
            System.out.println("@" + user.getScreenName() + " - <PROTECTED>");
        }            
    } catch (TwitterException te) {
        te.printStackTrace();
    }		
}
```

### Run the plug-in ###
To run the plug-in, it is necessary to create a _Launch configuration_ that provides an environment necessary to execute the plug-in (recall - Eclipse plug-in is an OSGi bundle which needs the OSGi framework to run).

There are several types of launch configurations (depends on the installed plug-ins):
  * Java application
  * Eclipse application
  * OSGi application
  * ...

Launch configuration can be used in two modes:
  * regular run
  * debugging

#### Create launch configuration ####
  * _Toolbar > Run > Launch configurations_
  * Create a new _Eclipse application_ launch configuration:
    1. tab - a new Eclipse instance workspace location
    1. tab - VM arguments
    1. tab - plugins which will be deployed into a new Eclipse (so called _target platform_):
      * default configuration is OK, but you can specify your own Eclipse configuration:
        * Select _Plug-ins selected below only_
        * Click on _Deselect all_
        * Select only `cz.cuni.mff.d3s.nprg044.twitter.api.test`
        * Click on _Add required plug-ins only_
          * only your plug-in and its dependencies will make a _target platform_ of the new Eclipse
  * click on _Run_

> |Hint #1: If your plug-in is not started then look at the status of the plug-in in the _Plug-in Registry_ view. To start a stopped plug-in, it is necessary to enable advanced properties in the context menu of the view.|
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

> |Hint #2: If your plug-in is not started, look at _Error View_ (_Window > Show View > Error Log_)|
|:-----------------------------------------------------------------------------------------------|

> |Hint #3: You can also use the OSGi console, to observe a state of the running Eclipse platform. See _Console View_, select _Open Console_ icon, option _Host OSGi Console_.|
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

> |Hint #4: For tracking plug-in dependencies, it is possible to use the views _Plug-ins_ or _Target Platform State_.|
|:-----------------------------------------------------------------------------------------------------------------|

### Debugging ###
  * _Toolbar >  Debug > Launch configurations_
  * The definition is the same as in the case of the _Run configuration_, or you can reuse an existing one.

  * Plug-in can be debugged in the same way as a regular Java-application.
  * You can
    * define breakpoints
    * observe the state of program variables
    * watch threads
    * ...


---


## Commands ##
A _command_ represents an activity which can be launched from different places in the Eclipse UI.
It is handled by a _handler_ according to a context (e.g., a selected object, active editor or a view).
Furthermore, a command can have associated key-bindings which are also activated according to a context.

> |Note: _Commands and handlers represent new approach which should replace the old approach of actions. However actions are still used._|
|:-------------------------------------------------------------------------------------------------------------------------------------|

Commands are defined via extension points:
  * `org.eclipse.ui.commands` - commands definition, commands category definition
  * `org.eclipse.ui.handlers` - definition of a handler for a given command. The handler has to implement the `IHandler` interface. It is recommended to inherit from the `AbstractHandler` class.
  * `org.eclipse.ui.bindings` - associate a given key shorcut with a command.
  * `org.eclipse.ui.menus` - define a location in the Eclipse UI where the command is shown.

#### Commands contribution ####
The extension point `org.eclipse.ui.menus` defines a place where the command contributes.
The location is defined via locationURI which have the structure `<scheme>:<menu-id>[?<placement-modifier>]`
The `scheme` can be:
  * menu: valid root of a menu, the constant `menu:org.eclipse.ui.main.menu` constant is defined for the main menu
  * toolbar: main toolbar has the constant `toolbar:org.eclipse.ui.main.toolbar`
  * popup: to contribute to a context pop-up menu (right-click). For contribution to all pop-up menus the constant `popup:org.eclipse.ui.popup.any` can be used

Placement-modifiers represents a position in the given menu/toolbar/popup - for example, `after=additions`, `before=window`.

## Plug-in defining commands: `cz.cuni.mff.d3s.nprg044.twitter.commands` ##
> |Goal: _Define a plug-in which will provide an action showing the user status_|
|:----------------------------------------------------------------------------|

  * create a new plug-in project `cz.cuni.mff.d3s.nprg044.twitter.commands`
  * tab _Dependencies_:
    * add the bundles `org.eclipse.core.commands` and `org.eclipse.ui`
  * tab _Extensions_:
    * add the extension `org.eclipse.ui.commands`
      * define a category `Twitter commands`
      * define a command `cz.cuni.mff.d3s.nprg044.twitter.commands.command.showUserStatus` with the name _Show user status_
        * setup the right category
    * add the extension `org.eclipse.ui.handlers`
      * define a new handler for the given command `cz.cuni.mff.d3s.nprg044.twitter.commands.command.showUserStatus`
      * implement the handler class (click on the link _class_):
```
public class ShowUserStatusHandler extends AbstractHandler {

	public ShowUserStatusHandler() {
	}

	/**
	 * the command has been executed, so we can extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the active workbench window
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		// get window shell ("window managed by the OS window manager")
		Shell shell = window.getShell();
		
		// create simple input dialog
		InputDialog inDialog = new InputDialog(shell, "Twitter status", "Write Twitter username:", "vtipy", null);
		
		// open the window and check result
		if (inDialog.open() == Dialog.OK) {
			// get the string written by the user
			String username = inDialog.getValue();
		
			// get twitter status of the user
			String status = getUserStatus(username);
		
			// show message to the user
			MessageDialog.openInformation(shell, "Twitter Status", status);
		}
		
		return null;
	}

	protected String getUserStatus(String username) {
		String result = null;
		
		try {
			// initialize twitter library and get the main handle
			Twitter twitter = TwitterAuthUtil.getTwitterInstance();
			
			// get information about the user
			User user = twitter.showUser(username);
			
			if (user.getStatus() != null) {
				// print user's status
				result = "@" + user.getScreenName() + " - " + user.getStatus().getText();
			} else {
				// the user is protected
				result = "@" + user.getScreenName() + " - <PROTECTED>";
			}
		} catch (TwitterException te) {
			te.printStackTrace();
		}
		
		return result;
	}
}
```
    * add the extension `org.eclipse.ui.bindings`
      * define a new _key_ (e.g., M1+6 means Ctrl+6)
    * add the extension `org.eclipse.ui.menus`
      * add new _menuContribution_
        * set _locatioURI_ to `menu:org.eclipse.ui.main.menu?after=additions`
        * add new _menu_ subelement called _Twitter_
          * add new _command_ subelement for the command `cz.cuni.mff.d3s.nprg044.twitter.commands.command.showUserStatus`
      * add another _menuContribution_
        * set _locationURI_ to `toolbar:org.eclipse.ui.main.toolbar?after=additions`
          * add new _toolbar_ subelement
            * add new _command_ subelement for command `cz.cuni.mff.d3s.nprg044.twitter.commands.command.showUserStatus`
              * define its icon and tool-tip

> |Note: _A good practice is to define elements' IDs hierarchically with meaningful parts, e.g., `cz.cuni.mff.d3s.nprg044.twitter.commands.AuthorizeUser`._|
|:-------------------------------------------------------------------------------------------------------------------------------------------------------|

See the reference documentation for the interface `org.eclipse.ui.ide.IIDEActionConstants` to find location URIs for the standard menus (File, Search, Window, etc).

### Your tasks (exercise) ###

  1. Try to add new sub-menus for timeline management and user-management
  1. Add the _About action_ into the main _Help_ menu.
  1. Add the _About action_ into all pop-up menus

## Useful Hints ##
It is useful to learn basic shortcuts to speed up Eclipse development:
  * Open type - _Ctrl+Shift+T_
  * Open resource - _Ctrl+Shift+R_
  * Show the inheritance tree of a selected element - _Ctrl+T_
  * Quick fix (on the line marked by error/warning) - _Ctrl+1_
  * Maximize editor - _Ctrl+M_
  * Go to line - _Ctrl+L_
  * Refactor the name - _Alt+Shift+R_
  * Open type's methods (in the editor) - _Ctrl+O_
  * Plug-in spy - shows more information about the selected element (ID, implementation class) - _Alt+Shift+F1_

Useful views:
  * Plug-in registry view - shows deployed plugins, their state, dependencies, allows to start/stop a plugin.


## Recommended reading & links ##

  * Lars Vogel: _Eclipse IDE  Tutorial_, http://www.vogella.de/articles/Eclipse/article.html
  * Lars Vogel: _Eclipse Shortcuts_, http://www.vogella.de/articles/EclipseShortcuts/article.html
  * Eclipse Wiki: Menu contributions (location URI), http://wiki.eclipse.org/Menu_Contributions