# Example: Twitter client #


## Scenario ##
Create a Twitter client as an updateable Eclipse application (PDE as well as standalone RCP). It should allow displaying users' statuses, posting a new message, etc.

## Wireframe ##
Application wireframe:
![http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/twitter-client-wireframe.png](http://svn.codespot.com/a/eclipselabs.org/nprg044-eclipse-platform/wiki/images/twitter-client-wireframe.png)

## Details ##

  * Twitter perspective
    * configure views
  * Wizard for account configuration
  * View for account list
  * View for timeline
    * support for multiple timelines (user timeline, friends timeline)
    * message
      * URL highlighting
  * Form-based editor for editing the post
    * computing the number of characters
    * attaching the photo
    * URL shortening
      * edit box which will query URL shortening services
  * Preferences to setup
    * Authentication token?
    * URL shortening service
      * predefined providers via extension points
    * image post service
      * predefined providers via extension points
  * bundle the application
    * help dialog
      * twitter4j logo
    * intro screen
    * create RCP application
  * create an update site
    * publish the update site on eclipse labs to allow updates
  * new plugin can define a new URL shortening service or image post service via extension points

# Design #
## Views ##

## Editor ##

## Commands ##