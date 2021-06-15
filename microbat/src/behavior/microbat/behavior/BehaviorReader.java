package microbat.behavior;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import microbat.util.Settings;

public class BehaviorReader {
	
	
	@SuppressWarnings("resource")
	public void readXLSX() throws IOException {
		
		String fileName = Settings.launchClass + "_behavior" + ".xlsx";
		
		File file = new File(fileName);
		if(file.exists()) {
			InputStream excelFileToRead = new FileInputStream(file);
			
			XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);
			
			XSSFSheet sheet = wb.getSheetAt(0);
			XSSFRow row;
			XSSFCell cell;
			
			Iterator<Row> rows = sheet.rowIterator();
			
			while (rows.hasNext()) {
				row = (XSSFRow) rows.next();
				
				if (row.getRowNum() > 0) {
					Behavior behavior = new Behavior();
					
					Iterator<Cell> cells = row.cellIterator();
					while (cells.hasNext()) {
						cell = (XSSFCell) cells.next();
						int i = cell.getColumnIndex();
						
						switch (i) {
						case 0:
							double wrongValueFeedback = cell.getNumericCellValue();
							behavior.setWrongValueFeedbacks((int)wrongValueFeedback);
							break;
						case 1:
							double wrongPathFeedback = cell.getNumericCellValue();
							behavior.setWrongPathFeedbacks((int)wrongPathFeedback);
							break;
						case 2:
							double correctFeedback = cell.getNumericCellValue();
							behavior.setCorrectFeedbacks((int)correctFeedback);
							break;
						case 3:
							double unclearFeedback = cell.getNumericCellValue();
							behavior.setUnclearFeedbacks((int)unclearFeedback);
							break;
						case 4:
							double skips = cell.getNumericCellValue();
							behavior.setSkips((int)skips);
							break;
						case 5:
							double additionalClicks = cell.getNumericCellValue();
							behavior.setAdditionalClickOnSteps((int)additionalClicks);
							break;
						case 6:
							double searchForward = cell.getNumericCellValue();
							behavior.setSearchForward((int)searchForward);
							break;
						case 7:
							double searchBackward = cell.getNumericCellValue();
							behavior.setSearchBackward((int)searchBackward);
							break;
						case 8:
							double undo = cell.getNumericCellValue();
							behavior.setUndo((int)undo);
							break;
						case 9:
							double generateTrace = cell.getNumericCellValue();
							behavior.setGenerateTrace((int)generateTrace);
							break;
						}
					}
					
					BehaviorData.projectBehavior.put(Settings.launchClass, behavior);
				}
				
			}
		}
		
	}
}
