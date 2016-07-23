package microbat.evaluation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import microbat.evaluation.model.Trial;

public class ExcelReporter {
	
	private String titles[] = {
			"test case",
    		"mutation file",
    		"mutation line",
    		"total steps",
    		"time",
    		
    		"jump steps (clear, loop)",
    		"jump steps (unclear, loop)",
    		"unclear number (loop)",
    		
    		"jump steps (clear, nonloop)",
    		"jump steps (unclear, nonloop)",
    		"unclear number (nonloop)",
    		
    		"result (clear, loop)",
    		"result (unclear, loop)",
    		"result (clear, nonloop)",
    		"result (unclear, nonloop)",
    		
    		"jump detail (clear, loop)",
    		"jump detail (unclear, loop)",
    		"jump detail (clear, nonloop)",
    		"jump detail (unclear, nonloop)"
    		};
	
	
	private File file;
	private Sheet sheet;
	private Workbook book;
	private int lastRowNum = 1;
	
	public ExcelReporter(String fileName){
		file = new File(fileName);
		
		if(file.exists()){
			InputStream excelFileToRead;
			try {
				excelFileToRead = new FileInputStream(file);
				book = new XSSFWorkbook(excelFileToRead);
				sheet = book.getSheetAt(0);
				
				lastRowNum = sheet.getPhysicalNumberOfRows();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			book = new XSSFWorkbook();
			sheet = book.createSheet("data");
			
			Row row = sheet.createRow(0);
			for(int i = 0; i < titles.length; i++){
	        	row.createCell(i).setCellValue(titles[i]); 
	        }
		}
	}

	public void export(Trial clearLoopTrial, Trial unclearLoopTrial, 
			Trial clearNonloopTrial, Trial unclearNonloopTrial) throws IOException{
		
		Row row = sheet.createRow(lastRowNum);
		fillRowInformation(row, clearLoopTrial, unclearLoopTrial, clearNonloopTrial, unclearNonloopTrial);
		
//		for(int i=0; i<titles.length; i++){
//        	sheet.autoSizeColumn(i);
//        }
		
        writeToExcel(book, file.getName());
        
        lastRowNum++;
	}
	
	private void fillRowInformation(Row row, Trial clearLoopTrial, Trial unclearLoopTrial,
			Trial clearNonloopTrial, Trial unclearNonloopTrial) {
		row.createCell(0).setCellValue(clearLoopTrial.getTestCaseName());
		row.createCell(1).setCellValue(clearLoopTrial.getMutatedFile());
		row.createCell(2).setCellValue(clearLoopTrial.getMutatedLineNumber());
		row.createCell(3).setCellValue(clearLoopTrial.getTotalSteps());
		row.createCell(4).setCellValue(clearLoopTrial.getTime());
		
		
		row.createCell(5).setCellValue(clearLoopTrial.getJumpSteps().size());
		row.createCell(6).setCellValue(unclearLoopTrial.getJumpSteps().size());
		row.createCell(7).setCellValue(unclearLoopTrial.getUnclearFeedbackNumber());
		
		row.createCell(8).setCellValue(clearNonloopTrial.getJumpSteps().size());
		row.createCell(9).setCellValue(unclearNonloopTrial.getJumpSteps().size());
		row.createCell(10).setCellValue(unclearNonloopTrial.getUnclearFeedbackNumber());
		
		row.createCell(11).setCellValue(clearLoopTrial.isBugFound());
		row.createCell(12).setCellValue(unclearLoopTrial.isBugFound());
		row.createCell(13).setCellValue(clearNonloopTrial.isBugFound());
		row.createCell(14).setCellValue(unclearNonloopTrial.isBugFound());
		
		row.createCell(15).setCellValue(clearLoopTrial.getJumpSteps().toString());
		row.createCell(16).setCellValue(unclearLoopTrial.getJumpSteps().toString());
		row.createCell(17).setCellValue(clearNonloopTrial.getJumpSteps().toString());
		row.createCell(18).setCellValue(unclearNonloopTrial.getJumpSteps().toString());
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
