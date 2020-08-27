package microbat;

import java.io.PrintStream;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.themes.ITheme;

/**
 * @author LLT
 * 
 */
public class MicrobatConsole extends MessageConsole implements IPropertyChangeListener {
	public static final String NAME = "Tzuyu Console";
	private static final String CONSOLE_FONT = "tzuyu.plugin.tester.consoleFont";
	private static MicrobatConsole console;
	
	public MicrobatConsole() {
		super(NAME, null, true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (CONSOLE_FONT.equals(event.getProperty())) {
			setConsoleFont();
		}
	}

	private void setConsoleFont() {
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					setConsoleFont();
				}
			});
		} else {
			ITheme theme = PlatformUI.getWorkbench().getThemeManager()
					.getCurrentTheme();
			Font font = theme.getFontRegistry().get(CONSOLE_FONT);
			console.setFont(font);
		}
	}

	public static MicrobatConsole showConsole() {
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		getConsole().activate();
		ITheme theme = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme();
		theme.addPropertyChangeListener(console);
		manager.showConsoleView(console);
		MessageConsoleStream stream = console.newMessageStream();
		stream.setActivateOnWrite(true);
		return console;
	}
	
	public static PrintStream getPrintStream() {
		MessageConsoleStream stream = getConsole().newMessageStream();
		stream.setActivateOnWrite(true);
		return new PrintStream(stream, true);
	}

	public static MicrobatConsole getConsole() {
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		boolean exists = false;
		if (console != null) {
			IConsole[] existing = manager.getConsoles();
			for (int i = 0; i < existing.length; i++) {
				if (console == existing[i]) {
					exists = true;
				}
			}
		} else {
			console = new MicrobatConsole();
		}
		if (!exists) {
			manager.addConsoles(new IConsole[] { console });
		}
		return console;
	}

	public static IOConsoleOutputStream getOutputStream() {
		IOConsoleOutputStream out = getConsole().newOutputStream();
		out.setActivateOnWrite(true);
		return out;
	}
	
	
}
