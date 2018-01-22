package microbat.codeanalysis.runtime;


import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
import org.eclipse.jdi.internal.connect.ConnectMessages;
import org.eclipse.jdi.internal.connect.SocketLaunchingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketListeningConnectorImpl;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;

import sav.strategies.dto.AppJavaClassPath;

@SuppressWarnings("restriction")
public class SocketLanchingConnector0 extends SocketLaunchingConnectorImpl {

	

	/** Time that a launched VM is given to connect to us. */
	private static final int ACCEPT_TIMEOUT = 30 * 1000;

	/**
	 * Home directory of the SDK or runtime environment used to launch the
	 * application.
	 */
	private String fHome;
	/** Launched VM options. */
	private String fOptions;
	/**
	 * Main class and arguments, or if -jar is an option, the main jar file and
	 * arguments.
	 */
	private String fMain;
	/** All threads will be suspended before execution of main. */
	private boolean fSuspend;
	/** Name of the Java VM launcher. */
	private String fLauncher;

	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketLanchingConnector0(
			VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);
	}

	/**
	 * @return Returns a short identifier for the connector.
	 */
	@Override
	public String name() {
		return "com.sun.jdi.CommandLineLaunch"; //$NON-NLS-1$
	}

	/**
	 * @return Returns a human-readable description of this connector and its
	 *         purpose.
	 */
	@Override
	public String description() {
		return ConnectMessages.SocketLaunchingConnectorImpl_Launches_target_using_Sun_Java_VM_command_line_and_attaches_to_it_13;
	}

	/**
	 * Retrieves connection arguments.
	 */
	private void getConnectionArguments(Map<String,? extends Connector.Argument> connectionArgs)
			throws IllegalConnectorArgumentsException {
		String attribute = ""; //$NON-NLS-1$
		try {
			attribute = "home"; //$NON-NLS-1$
			fHome = ((Connector.StringArgument) connectionArgs.get(attribute))
					.value();
			attribute = "options"; //$NON-NLS-1$
			fOptions = ((Connector.StringArgument) connectionArgs
					.get(attribute)).value();
			attribute = "main"; //$NON-NLS-1$
			fMain = ((Connector.StringArgument) connectionArgs.get(attribute))
					.value();
			attribute = "suspend"; //$NON-NLS-1$
			fSuspend = ((Connector.BooleanArgument) connectionArgs
					.get(attribute)).booleanValue();
			attribute = "quote"; //$NON-NLS-1$
			((Connector.StringArgument) connectionArgs.get(attribute)).value();
			attribute = "vmexec"; //$NON-NLS-1$
			fLauncher = ((Connector.StringArgument) connectionArgs
					.get(attribute)).value();
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketLaunchingConnectorImpl_Connection_argument_is_not_of_the_right_type_14,
					attribute);
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketLaunchingConnectorImpl_Necessary_connection_argument_is_null_15,
					attribute);
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketLaunchingConnectorImpl_Connection_argument_is_not_a_number_16,
					attribute);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.LaunchingConnector#launch(java.util.Map)
	 */
	public VirtualMachine launch(Map<String,? extends Connector.Argument> connectionArgs, File workingDirectory, AppJavaClassPath configuration) throws IOException,
			IllegalConnectorArgumentsException, VMStartException {
		getConnectionArguments(connectionArgs);

		// A listening connector is used that waits for a connection of the VM
		// that is started up.
		// Note that port number zero means that a free port is chosen.
		SocketListeningConnectorImpl listenConnector = new SocketListeningConnectorImpl(
				virtualMachineManager());
		Map<String, Connector.Argument> args = listenConnector.defaultArguments();
		((Connector.IntegerArgument) args.get("timeout")).setValue(ACCEPT_TIMEOUT); //$NON-NLS-1$
		String address = listenConnector.startListening(args);

		// String for Executable.
		String slash = System.getProperty("file.separator"); //$NON-NLS-1$
		String execString = fHome + slash + "bin" + slash + fLauncher; //$NON-NLS-1$

		// Add Debug options.
		//execString += " -Xdebug -Xnoagent -Djava.compiler=NONE"; //$NON-NLS-1$
		//execString += " -Xrunjdwp:transport=dt_socket,address=" + address + ",server=n,suspend=" + (fSuspend ? "y" : "n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		execString += " -noverify -agentlib:jdwp=transport=dt_socket,address="+ address + ",suspend=" + (fSuspend ? "y" : "n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		// Add User specified options.
		if (fOptions != null)
			execString += " " + fOptions; //$NON-NLS-1$

//		execString += " -javaagent:" + configuration.getAgentLib();
		
		// Add Main class.
		execString += " " + fMain; //$NON-NLS-1$
		System.out.println(execString);
		
		// Start VM.
		String[] cmdLine = DebugPlugin.parseArguments(execString);
		Process proc = Runtime.getRuntime().exec(cmdLine, new String[]{}, workingDirectory);

		// The accept times out if the VM does not connect.
		VirtualMachineImpl virtualMachine;
		try {
			virtualMachine = (VirtualMachineImpl) listenConnector.accept(args);
		} catch (InterruptedIOException e) {
			proc.destroy();
			String message = NLS.bind(ConnectMessages.SocketLaunchingConnectorImpl_VM_did_not_connect_within_given_time___0__ms_1,
							new String[] { ((Connector.IntegerArgument) args
									.get("timeout")).value() }); //$NON-NLS-1$ 
			throw new VMStartException(message, proc);
		}

		virtualMachine.setLaunchedProcess(proc);
		virtualMachine.setRequestTimeout(ACCEPT_TIMEOUT);
		return virtualMachine;
	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free
	 * port.
	 * 
	 * @return a free port number on localhost, or -1 if unable to find a free
	 *         port
	 * @since 3.2
	 */
	public static int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

}
