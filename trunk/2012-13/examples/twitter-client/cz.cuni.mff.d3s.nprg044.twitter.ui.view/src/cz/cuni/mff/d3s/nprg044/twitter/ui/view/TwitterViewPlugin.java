package cz.cuni.mff.d3s.nprg044.twitter.ui.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TwitterViewPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "cz.cuni.mff.d3s.nprg044.twitter.ui.view"; //$NON-NLS-1$

	// The shared instance
	private static TwitterViewPlugin plugin;
	
	public TwitterViewPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 */
	public static TwitterViewPlugin getDefault() {
		return plugin;
	}
}
