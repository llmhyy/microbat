package microbat.evaluation.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import microbat.evaluation.model.Trial;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReporter {
	
	private Workbook book; 
	private Sheet sheet;
	
	private int bookSize = 0;
	
	public ExcelReporter(){
		this.bookSize = 0;
	}

	public void start(){
		book = new XSSFWorkbook(); 
		sheet = book.createSheet("data");  
        Row row = sheet.createRow((short) 0); 
        
        String titles[] = {"test case",
        		"is bug found",
        		"total steps",
        		"jump steps",
        		"mutated file",
        		"mutated line number",
        		"jump steps",
        		"result",
        		"time"};
        for(int i = 0; i < titles.length; i++){
        	row.createCell(i).setCellValue(titles[i]); 
        }
        
        bookSize = 1;
	}
	
	public void export(List<Trial> trials, String fileName){
		int rowNo = 1;
        try {
        	for(Trial trial: trials){
        		Row row = sheet.createRow(rowNo);
        		
        		row.createCell(0).setCellValue(trial.getTestCaseName());
        		row.createCell(1).setCellValue(trial.isBugFound());
        		row.createCell(2).setCellValue(trial.getTotalSteps());
        		
        		if(trial.getJumpSteps() != null){
        			row.createCell(3).setCellValue(trial.getJumpSteps().size());        			
        		}
        		
        		row.createCell(4).setCellValue(trial.getMutatedFile());
        		row.createCell(5).setCellValue(trial.getMutatedLineNumber());
        		
        		if(trial.getJumpSteps() != null){
        			row.createCell(6).setCellValue(trial.getJumpSteps().toString());   
        			
        			if(trial.getJumpSteps().size() == 183){
        				int i=0;
        				for(String step: trial.getJumpSteps()){
        					if(step.contains("unclear")){
        						i++;
        					}
        				}
        				System.out.println("unclear number: " + i);
        			}
        		}
        		
        		row.createCell(7).setCellValue(trial.getResult());
        		row.createCell(8).setCellValue(trial.getTime());
        		
        		rowNo++;
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        for(int i=0; i<7; i++){
        	sheet.autoSizeColumn(i);
        }
        
        writeToExcel(fileName);
	}

	private void writeToExcel(String fileName){
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName + ".xlsx");
			book.write(fileOut); 
			fileOut.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
