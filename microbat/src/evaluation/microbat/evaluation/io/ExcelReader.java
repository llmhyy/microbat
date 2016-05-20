package microbat.evaluation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import microbat.evaluation.model.Trial;
import microbat.util.Settings;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
	
	private Set<Trial> set = new HashSet<>();
	
	public int readXLSX() throws IOException {
		
		String projectName = Settings.projectName;
		int num = 0;

		String fileName = projectName + num + ".xlsx";
		
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
						case 4:
							String mutatedFile = cell.getStringCellValue();
							trial.setMutatedFile(mutatedFile);
							break;
						case 5:
							int linNum = (int) cell.getNumericCellValue();
							trial.setMutatedLineNumber(linNum);
							break;
						case 7:
							String result = cell.getStringCellValue();
							trial.setResult(result);
							break;
						}
					}
					
					getSet().add(trial);
				}
				
			}

			num++;
			fileName = projectName + num + ".xlsx";
			file = new File(fileName);
		}

		return num;
	}

	public Set<Trial> getSet() {
		return set;
	}

	public void setSet(Set<Trial> set) {
		this.set = set;
	}
	
}
