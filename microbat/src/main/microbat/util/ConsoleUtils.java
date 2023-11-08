/**
 * 
 */
package microbat.util;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author knightsong
 *
 */
public class ConsoleUtils {
	static MessageConsole console = null;
	static MessageConsoleStream consoleStream = null;
	static IConsoleManager consoleManager = null;
	static final String CONSOLE_NAME = "DebugConsole";

	private static ConsoleUtils singleton = new ConsoleUtils();

	public void setItselfAsSingleton() {
		singleton = this;
	}

	protected void initConsole() {
		console = new MessageConsole(CONSOLE_NAME, null);
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoles(new IConsole[] { console });
		consoleStream = console.newMessageStream();
	}

	public static void printMessage(String message) {
		if (message != null) {
			if (console == null) {
				singleton.initConsole();
			}
			singleton.printMessageInstance(message);
		}

	}
	
	protected void printMessageInstance(String message) {
		consoleManager.showConsoleView(console);
		consoleStream.print(message + "\n");
	}
}
