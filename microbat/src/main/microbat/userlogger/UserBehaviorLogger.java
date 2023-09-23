package microbat.userlogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Display;

import microbat.Activator;
import microbat.preference.MicrobatPreference;
import microbat.util.Settings;

public class UserBehaviorLogger {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String dilimeter = ",";
	
	protected static String logPath = null;
	
	public static void logEvent(final UserBehaviorType type) {
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				logPath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.LOG_PATH_KEY);
			}
		});
		
		System.out.println(logPath);
		createFileIfNotExist();
		
		String log = genLog(type);
		try (FileWriter writer = new FileWriter(logPath, true)) {
			writer.write(log);
			writer.write('\n');
			writer.close();
		} catch (IOException e) {
			System.out.println("Fail to write log");
		}
	}
	
	protected static void createFileIfNotExist() {
        File file = new File(logPath);
        if (!file.exists()) {
            try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
	}
	
	protected static String genLog(final UserBehaviorType type) {
		StringBuilder stringBuilder = new StringBuilder();
		
		Date currentDate = new Date();
		stringBuilder.append(dateFormat.format(currentDate));
		stringBuilder.append(dilimeter);
		
		stringBuilder.append(type.id);
		stringBuilder.append(dilimeter);
		
		stringBuilder.append(type.description);
		stringBuilder.append(dilimeter);
		
		return stringBuilder.toString();
	}
}
