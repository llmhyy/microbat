package sav.strategies.vm;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * The code of this class is copied from eclipse JDT project, in order to make the whole Ziyuan project works
 * in eclipse plugin.
 * 
 * @author "linyun"
 *
 */
public class BootstrapPlugin {
	private static com.sun.jdi.VirtualMachineManager fVirtualMachineManager;

	public BootstrapPlugin() {
	}

	public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
		if (fVirtualMachineManager != null)
			return fVirtualMachineManager;

		try {
			IExtensionRegistry extensionRegistry = Platform
					.getExtensionRegistry();
			String className = null;
			if (extensionRegistry != null) { // is null if the platform was not
												// started
				className = extensionRegistry
						.getExtensionPoint(
								JDIDebugPlugin.getUniqueIdentifier(),
								"jdiclient").getLabel(); //$NON-NLS-1$
			}
			Class<?> clazz = null;
			if (className != null) {
				clazz = Class.forName(className);
			}
			if (clazz != null) {
				fVirtualMachineManager = (com.sun.jdi.VirtualMachineManager) clazz
						.newInstance();
			}
		} catch (ClassNotFoundException e) {
		} catch (NoClassDefFoundError e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		if (fVirtualMachineManager == null) {
			// If any exceptions occurred, we'll end up here
			fVirtualMachineManager = new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		}

		return fVirtualMachineManager;
	}
}

