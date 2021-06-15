package microbat.behavior;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import microbat.util.Settings;

public class BehaviorReporter {
	
	private File file;
	private Sheet sheet;
	private Workbook book;
	private int lastRowNum = 1;
	
	public BehaviorReporter(String fileName){
		
		if(BehaviorSettings.enableBehaviorRecording){
			fileName = fileName + "_behavior" + ".xlsx";
			file = new File(fileName);
			
			if(file.exists()){
				InputStream excelFileToRead;
				try {
					excelFileToRead = new FileInputStream(file);
					book = new XSSFWorkbook(excelFileToRead);
					sheet = book.getSheetAt(0);
					
//				lastRowNum = sheet.getPhysicalNumberOfRows();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				book = new XSSFWorkbook();
				sheet = book.createSheet("data");
				
				List<String> titles = new ArrayList<>();
				titles.add("wrong value feedback");
				titles.add("wrong path feedback");
				titles.add("correct feedback");
				titles.add("unclear feedback");
				titles.add("skips");
				titles.add("additional clicks");
				titles.add("search forward");
				titles.add("search backward");
				titles.add("undo");
				titles.add("generate trace");
				
				Row row = sheet.createRow(0);
				for(int i = 0; i < titles.size(); i++){
					row.createCell(i).setCellValue(titles.get(i)); 
				}
			}
			
		}
		
	}
	
	public void export(HashMap<String, Behavior> data) {
		if(BehaviorSettings.enableBehaviorRecording){
			Row row = sheet.createRow(lastRowNum);
			fillRowInformation(row, data);
			writeToExcel(book, file.getName());
		}
	}

	private void fillRowInformation(Row row, HashMap<String, Behavior> data) {
		Behavior behavior = data.get(Settings.launchClass);
		row.createCell(0).setCellValue(behavior.getWrongValueFeedbacks());
		row.createCell(1).setCellValue(behavior.getWrongPathFeedbacks());
		row.createCell(2).setCellValue(behavior.getCorrectFeedbacks());
		row.createCell(3).setCellValue(behavior.getUnclearFeedbacks());
		row.createCell(4).setCellValue(behavior.getSkips());
		row.createCell(5).setCellValue(behavior.getAdditionalClickOnSteps());
		row.createCell(6).setCellValue(behavior.getSearchForward());
		row.createCell(7).setCellValue(behavior.getSearchBackward());
		row.createCell(8).setCellValue(behavior.getUndo());
		row.createCell(9).setCellValue(behavior.getGenerateTrace());
	}

	private void writeToExcel(Workbook book, String fileName){
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
			book.write(fileOut); 
			fileOut.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	
}
