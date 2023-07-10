package microbat.vectorization.vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class LibraryClassDetector {
	
	private static final String LIBRARY_CLASSES_PATH = "./src/main/microbat/probability/SPP/vectorization/vector/java_11_classes.txt";
	private static final Set<String> LIBRARY_CLASSES = LibraryClassDetector.readLibraryClasses();
	
	public static boolean isLibClass(final String type) {
		return LibraryClassDetector.LIBRARY_CLASSES.contains(type);
	}
	
	private static Set<String> readLibraryClasses() {
		Set<String> classes = new HashSet<>();
		BufferedReader reader;
		
		String basePath = System.getProperty("user.dir");
		String classes_path = Paths.get(basePath, LibraryClassDetector.LIBRARY_CLASSES_PATH).toString();
		
		try {
			reader = new BufferedReader(new FileReader(classes_path));
			String line = reader.readLine();
			while (line != null) {
				line = line.replace(".", "/");
				classes.add(line);
				line = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot read the library classes");
		}
	
		return classes;
	}
}
