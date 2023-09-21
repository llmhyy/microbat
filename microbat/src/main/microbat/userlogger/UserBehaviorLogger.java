package microbat.userlogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserBehaviorLogger {
	
	private static final String RELATIVE_PATH = "user_behavior_log.txt";
	private static final String ABSOLUTE_PATH = Paths.get(RELATIVE_PATH).toAbsolutePath().toString();
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String dilimeter = ",";
	
	public static void logEvent(final UserBehaviorType type) {

      
		createFileIfNotExist();
		
		String log = genLog(type);
		try (FileWriter writer = new FileWriter(ABSOLUTE_PATH, true)) {
			writer.write(log);
			writer.write('\n');
			writer.close();
		} catch (IOException e) {
			System.out.println("Fail to write log");
		}
	}
	
	protected static void createFileIfNotExist() {
        File file = new File(ABSOLUTE_PATH);
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
