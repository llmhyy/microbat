package microbat.evaluation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import microbat.evaluation.model.Trial;

public class ExcelReporter {
	
	private File file;
	private Sheet sheet;
	private Workbook book;
	private int lastRowNum = 1;
	
	public ExcelReporter(String fileName, double[] unclearRates){
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
			
			List<String> titles = new ArrayList<>();
			titles.add("test case");
			titles.add("mutation file");
			titles.add("mutation line");
			titles.add("total steps");
			titles.add("time");
			
			for(int i=0; i<unclearRates.length; i++){
				String nonLoopJumpSteps = "jump steps (nonloop, " + unclearRates[i] + ")";
				String loopJumpSteps = "jump steps (loop, " + unclearRates[i] + ")";
				String nonLoopUnclearSteps = "unclear steps (nonloop, " + unclearRates[i] + ")";
				String loopUnclearSteps = "unclear steps (loop, " + unclearRates[i] + ")";
				String nonLoopResult = "result (nonloop, " + unclearRates[i] + ")";
				String loopResult = "result (loop, " + unclearRates[i] + ")";
				String nonLoopDetail = "detail (nonloop, " + unclearRates[i] + ")";
				String loopDetail = "detail (loop, " + unclearRates[i] + ")";
				
				titles.add(nonLoopJumpSteps);
				titles.add(loopJumpSteps);
				titles.add(nonLoopUnclearSteps);
				titles.add(loopUnclearSteps);
				titles.add(nonLoopResult);
				titles.add(loopResult);
				titles.add(nonLoopDetail);
				titles.add(loopDetail);
			}
			
			Row row = sheet.createRow(0);
			for(int i = 0; i < titles.size(); i++){
	        	row.createCell(i).setCellValue(titles.get(i)); 
	        }
		}
	}
	
	public void export(List<Trial> trialList) {
		Row row = sheet.createRow(lastRowNum);
		
		fillRowInformation(row, trialList);
		
		writeToExcel(book, file.getName());
        
        lastRowNum++;
	}

	private void fillRowInformation(Row row, List<Trial> trialList) {
		Trial trial = trialList.get(0);
		row.createCell(0).setCellValue(trial.getTestCaseName());
		row.createCell(1).setCellValue(trial.getMutatedFile());
		row.createCell(2).setCellValue(trial.getMutatedLineNumber());
		row.createCell(3).setCellValue(trial.getTotalSteps());
		row.createCell(4).setCellValue(trial.getTime());
		
		int column = 5;
		for(int i=0; i<trialList.size(); i=i+2){
			Trial nonLoopTrial = trialList.get(i);
			Trial loopTrial = trialList.get(i+1);
			
			row.createCell(column).setCellValue(nonLoopTrial.getJumpSteps().size());
			row.createCell(column+1).setCellValue(loopTrial.getJumpSteps().size());
			row.createCell(column+2).setCellValue(nonLoopTrial.getUnclearFeedbackNumber());
			row.createCell(column+3).setCellValue(loopTrial.getUnclearFeedbackNumber());
			row.createCell(column+4).setCellValue(nonLoopTrial.getResult());
			row.createCell(column+5).setCellValue(loopTrial.getResult());
			row.createCell(column+6).setCellValue(nonLoopTrial.getJumpSteps().toString());
			row.createCell(column+7).setCellValue(loopTrial.getJumpSteps().toString());
			
			column = column + 8;
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
