package microbat.evaluation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import microbat.evaluation.model.Trial;
import microbat.util.Settings;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHandler {
	public List<Trial> readXLSX(String path) throws IOException {
		
		List<Trial> trials = new ArrayList<>();
		
//		String projectName = "apache-common-math-2.2";
		
		String projectName = Settings.projectName;
		int num = 0;

		
		String fileName = path + projectName + num + ".xlsx";
		
		File file = new File(fileName);
		while (file.exists()) {
			InputStream excelFileToRead = new FileInputStream(file);
			
			@SuppressWarnings("resource")
			XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

			XSSFSheet sheet = wb.getSheetAt(0);
			XSSFRow row;
			XSSFCell cell;

			Iterator<Row> rows = sheet.rowIterator();

			while (rows.hasNext()) {
				row = (XSSFRow) rows.next();

				if (row.getRowNum() > 0) {
					Trial trial = new Trial();
					
					Iterator<Cell> cells = row.cellIterator();
					while (cells.hasNext()) {
						cell = (XSSFCell) cells.next();
						int i = cell.getColumnIndex();
						
						switch (i) {
						case 0:
							String testcaseName = cell.getStringCellValue();
							trial.setTestCaseName(testcaseName);
							break;
						case 1:
							boolean isBugFound = cell.getBooleanCellValue();
							trial.setBugFound(isBugFound);
							break;
						case 2:
							int totalSteps = (int) cell.getNumericCellValue();
							trial.setTotalSteps(totalSteps);
							break;
						case 3:
							//do nothing
							break;
						case 4:
							String mutatedFile = cell.getStringCellValue();
							trial.setMutatedFile(mutatedFile);
							break;
						case 5:
							int linNum = (int) cell.getNumericCellValue();
							trial.setMutatedLineNumber(linNum);
							break;
						case 6:
							String jumpStrings = cell.getStringCellValue();
							String trimString = jumpStrings.substring(1, jumpStrings.length()-1);
							String[] steps = trimString.split(",");
							List<String> jumpSteps = new ArrayList<>();
							for(String step: steps){
								jumpSteps.add(step);
							}
							trial.setJumpSteps(jumpSteps);
							break;
						case 7:
							String result = cell.getStringCellValue();
							trial.setResult(result);
							break;
						}
					}
					
					trials.add(trial);
				}
				
			}

			num++;
			fileName = path + projectName + num + ".xlsx";
			file = new File(fileName);
		}

		return trials;
	}
	
	public void start() {
		String path = "F:\\Documents\\Acamedic_Postdoc\\paper_writing\\microbat\\source\\data\\ant\\";
		try {
			List<Trial> trials = readXLSX(path);
			ExcelReporter writer = new ExcelReporter();
			writer.start();
			writer.export(trials, Settings.projectName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public List<Trial> readXLSX0(String path, String projectName) throws IOException {
		
		List<Trial> trials = new ArrayList<>();
		
//		String projectName = "apache-common-math-2.2";
		
		String fileName = path + projectName + ".xlsx";
		
		File file = new File(fileName);
		if (file.exists()) {
			InputStream excelFileToRead = new FileInputStream(file);
			
			@SuppressWarnings("resource")
			XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

			XSSFSheet sheet = wb.getSheetAt(0);
			XSSFRow row;
			XSSFCell cell;

			Iterator<Row> rows = sheet.rowIterator();

			while (rows.hasNext()) {
				row = (XSSFRow) rows.next();

				if (row.getRowNum() > 0) {
					Trial trial = new Trial();
					
					Iterator<Cell> cells = row.cellIterator();
					while (cells.hasNext()) {
						cell = (XSSFCell) cells.next();
						int i = cell.getColumnIndex();
						
						switch (i) {
						case 0:
							String testcaseName = cell.getStringCellValue();
							trial.setTestCaseName(testcaseName);
							break;
						case 1:
							boolean isBugFound = cell.getBooleanCellValue();
							trial.setBugFound(isBugFound);
							break;
						case 2:
							int totalSteps = (int) cell.getNumericCellValue();
							trial.setTotalSteps(totalSteps);
							break;
						case 3:
							//do nothing
							break;
						case 4:
							String mutatedFile = cell.getStringCellValue();
							trial.setMutatedFile(mutatedFile);
							break;
						case 5:
							int linNum = (int) cell.getNumericCellValue();
							trial.setMutatedLineNumber(linNum);
							break;
						case 6:
							String jumpStrings = cell.getStringCellValue();
							String trimString = jumpStrings.substring(1, jumpStrings.length()-1);
							String[] steps = trimString.split(",");
							List<String> jumpSteps = new ArrayList<>();
							for(String step: steps){
								jumpSteps.add(step);
							}
							trial.setJumpSteps(jumpSteps);
							break;
						case 7:
							String result = cell.getStringCellValue();
							trial.setResult(result);
							break;
						}
					}
					
					trials.add(trial);
				}
				
			}
		}

		return trials;
	}

	public void start2(){
		String path = "";
		
		String[] projects = new String[]{"apache-ant-1.9.6", "apache-common-math-2.2", "apache-collections-3.2.2"};
		
		List<Trial> cleanTrials = new ArrayList<>();
		
		for(String projectName: projects){
			try {
				List<Trial> trials = readXLSX0(path, projectName);
				
				Iterator<Trial> iterator = trials.iterator();
				while(iterator.hasNext()){
					Trial trial = iterator.next();
					if(!trial.isBugFound()){
						iterator.remove();
					}
				}
				
				ExcelReporter writer = new ExcelReporter();
				writer.start();
				writer.export(trials, projectName + "-clean");
				
				
				cleanTrials.addAll(trials);
				
				int numberOfDistinctTestCases = findDistinctTestCase(trials);
				System.out.println(numberOfDistinctTestCases);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		Trial maxTrial = null;
		for(Trial trial: cleanTrials){
			if(maxTrial == null){
				maxTrial = trial;
			}
			else if(trial.getJumpSteps().size() > maxTrial.getJumpSteps().size()){
				maxTrial = trial;
			}
		}
		
		ExcelReporter writer = new ExcelReporter();
		writer.start();
		writer.export(cleanTrials, "apache-total-clean");
	}

	private int findDistinctTestCase(List<Trial> trials) {
		List<String> testcases = new ArrayList<>();
		for(Trial trial: trials){
			String testcase = trial.getTestCaseName();
			if(!testcases.contains(testcase)){
				testcases.add(testcase);
			}
		}
		return testcases.size();
	}
}
