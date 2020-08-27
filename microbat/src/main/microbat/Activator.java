package microbat;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import microbat.views.ImageUI;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "microbat"; //$NON-NLS-1$
	
	public static String tempVariableName = "microbat_tmp_var";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		ImageRegistry imgReg = getImageRegistry();
		imgReg.put(ImageUI.CHECK_MARK, getImageDescriptor(ImageUI.CHECK_MARK));
		imgReg.put(ImageUI.WRONG_VALUE_MARK, getImageDescriptor(ImageUI.WRONG_VALUE_MARK));
		imgReg.put(ImageUI.WRONG_PATH_MARK, getImageDescriptor(ImageUI.WRONG_PATH_MARK));
		imgReg.put(ImageUI.QUESTION_MARK, getImageDescriptor(ImageUI.QUESTION_MARK));
		imgReg.put(ImageUI.UNDO_MARK, getImageDescriptor(ImageUI.UNDO_MARK));
		
		System.setOut(MicrobatConsole.getPrintStream());
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		try {
//			URL installURL = getDefault().getDescriptor().getInstallURL();
			URL installURL = plugin.getBundle().getEntry("/");
			URL url = new URL(installURL, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * After an execution, its trace should be kept in this field.  
	 */
//	private Trace currentTrace = new Trace();
//
//	public Trace getCurrentTrace() {
//		return currentTrace;
//	}
//
//	public void setCurrentTrace(Trace currentTrace) {
//		this.currentTrace = currentTrace;
//	}

	
	
}
