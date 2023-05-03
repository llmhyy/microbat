package microbat.util;

public class ConsoleUtilsStub extends ConsoleUtils {
	
	@Override
	protected void initConsole() {
		// NOOP
	}
	
	@Override
	protected void printMessageInstance(String message) {
        if (message != null) {
        	System.out.println(message);
        }
    }
}
