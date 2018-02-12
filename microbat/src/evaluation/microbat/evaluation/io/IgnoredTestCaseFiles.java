package microbat.evaluation.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * This class is used to ignore the test cases which is either failure or cannot
 * have proper mutation
 * 
 * @author "linyun"
 * 
 */
public class IgnoredTestCaseFiles {
	private String fileName = "ignored_test_case.txt";
	private HashSet<String> set = new HashSet<>();

	public IgnoredTestCaseFiles() {
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		initializeIgnoreSet();
	}

	private void initializeIgnoreSet() {
		String line = null;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				set.add(line);
			}
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}

	}
	
	public void addTestCase(String testcase){
		if(!contains(testcase)){
			this.set.add(testcase);
			writeIgnoreSetIntoFile();
		}
	}
	
	public boolean contains(String testcase){
		return this.set.contains(testcase);
	}

	public void writeIgnoreSetIntoFile() {
		try {
			// Assume default encoding.
			FileWriter fileWriter = new FileWriter(fileName);

			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			// Note that write() does not automatically
			// append a newline character.
			for(String testcase: set){
				bufferedWriter.write(testcase);
				bufferedWriter.newLine();
			}

			// Always close files.
			bufferedWriter.close();
		} catch (IOException ex) {
			System.out.println("Error writing to file '" + fileName + "'");
		}
	}

}
