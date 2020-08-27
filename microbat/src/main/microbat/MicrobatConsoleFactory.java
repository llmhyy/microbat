package microbat;

import org.eclipse.ui.console.IConsoleFactory;

/**
 * @author LLT
 *
 */
public class MicrobatConsoleFactory implements IConsoleFactory {

	@Override
	public void openConsole() {
		MicrobatConsole.showConsole();
	}

}
