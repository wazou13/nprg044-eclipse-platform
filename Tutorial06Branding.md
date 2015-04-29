# Introduction #


# Help #

We will create a simple user help for the Twitter client. A typical approach is to use a separate plugin.
Before you start creating the user help, you need to have some raw HTML files with the actual content.

Create a new plugin `cz.cuni.mff.d3s.nprg044.twitter.help`.

## Help TOC ##

The next step is to define the structure for the user help.
You need to create special _table of contents (toc)_ files - either one large file for the whole application or a hierarchy containing a file for each category (sub-topic).

Here we use a small hierarchy created with a mixed top-down and bottom-up approach.
The basic structure is defined using a top-down approach - see the `<link toc=... />` elements.
The bottom-up approach is represented here by the `<anchor>` elements that indicate locations where other plugins can contribute something.

```
<toc label="T4Eclipse Documentation">
	<anchor id="pre" />
	
	<topic label="T4Eclipse User Guide">
		<link toc="help/toc-user.xml" />
	</topic>
	
	<anchor id="additions-user" />
	
	<topic label="T4Eclipse Developer Guide">
		<link toc="help/toc-devel.xml" />
	</topic>		
	
	<anchor id="additions-devel" />
	
	<anchor id="additions" />
	
	<anchor id="post" />
</toc>
```

Add a new extension `org.eclipse.help.toc` and specify all toc files. Exactly one toc file must be always tagged as primary.

# Feature #
Set of plugins with a specific meta-information.

  * new _Feature project_
  * define the ID `cz.cuni.mff.d3s.nprg044.twitter.feature`
  * initialize the feature with all the plugins `cz.cuni.mff.d3s.nprg044.twitter.*`
  * it is possible to specify
    * feature description
    * licencing
    * included plugins/features
      * it is possible to specify whether the plugin should be unpacked or not (this is important for included libraries)
    * dependencies
      * can be computed automatically on included plugins
    * dedicated installation
      * the case of binary plugins

# Product #

An Eclipse based _product_ is a stand-alone program built with the Eclipse platform. It can bundle multiple features and plugins.

  * create a new plugin `cz.cuni.mff.d3s.nprg044.twitter.branding`
  * create a new product configuration called `twitter.product`
    * it is used to configure properties of the extension point `org.eclipse.core.runtime.products`
    * do not forget to click on the _Synchronize_ link
    * _ID_ = `cz.cuni.mff.d3s.nprg044.twitter.branding.TwitterClient`
    * _Name_ = `Twitter Client`
    * the product is based on _features_
    * create a new product definition:
      * _Product ID_ = `TwitterClient`
        * product identification is composed of _Product ID_ and defining plugin's ID
      * _Application_ = `org.eclipse.ui.ide.workbench`
      * synchronize and look into `plugin.xml` that the extension point `org.eclipse.core.runtime.products` is configured properly
        * there are various properties which are defined in `IWorkbenchPreferenceConstants` and `IProductConstants`
    * add `cz.cuni.mff.d3s.nprg044.twitter.feature` to _Dependencies_
    * change launcher name on the _Launch_ tab

|The location, size, and color of the progress bar and progress message shown in the splash screen during startup can be configured using the properties `startupProgressRect`, `startupMessageRect`, and `startupForegroundColor`. See `IProductConstants` for more information about these properties. Note that by default, no progress will be reported at startup. To enable startup progress reporting, set the following preference to true in the preference customization file: `IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP`|
|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

A product has to be associated with an _application_. It represents the default entry point for the product when it is running.
Eclipse platform is represented by `org.eclipse.ui.ide.workbench`. However, it is possible to create a new application. See the example.

## Splash screen ##
Normally, `splash.bmp` figure is shown if it is located in the root directory of the product's plugin. However, splash can be hooked also via the extension point `org.eclipse.ui.splashHandlers`.

# Update site #
  * create a new project `cz.cuni.mff.d3s.nprg044.twitter.update-site`
  * setup the update site
    * included features
    * titles
  * build the update site
  * try to install the feature from the update site

# Resources #
  * Eclipse Help - Product customization - http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/guide/product_configproduct.htm
  * Eclipse Article - Branding your Application - http://www.eclipse.org/articles/Article-Branding/branding-your-application.html
  * IBM developerworks - **http://www.ibm.com/developerworks/opensource/library/os-ecfeat/index.html
  * Lars Vogella - RCP Tutorial - http://www.vogella.de/articles/EclipseRCP/article.html**