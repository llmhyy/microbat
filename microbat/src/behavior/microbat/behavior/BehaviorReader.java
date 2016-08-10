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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public class BehaviorReader {
	
	private String[] getProjectsInWorkspace(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		
		String[] projectStrings = new String[projects.length];
		for(int i=0; i<projects.length; i++){
			projectStrings[i] = projects[i].getName();
		}
		
		return projectStrings;
	}
	
	@SuppressWarnings("resource")
	public void readXLSX() throws IOException {
		
		String[] projectNames = getProjectsInWorkspace();
		for(String projectName: projectNames){
			String fileName = projectName + "_behavior" + ".xlsx";
			
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
								String wrongValueFeedback = cell.getStringCellValue();
								behavior.setWrongValueFeedbacks(Integer.valueOf(wrongValueFeedback));
								break;
							case 1:
								String wrongPathFeedback = cell.getStringCellValue();
								behavior.setWrongPathFeedbacks(Integer.valueOf(wrongPathFeedback));
								break;
							case 2:
								String correctFeedback = cell.getStringCellValue();
								behavior.setCorrectFeedbacks(Integer.valueOf(correctFeedback));
								break;
							case 3:
								String unclearFeedback = cell.getStringCellValue();
								behavior.setUnclearFeedbacks(Integer.valueOf(unclearFeedback));
								break;
							case 4:
								String skips = cell.getStringCellValue();
								behavior.setSkips(Integer.valueOf(skips));
								break;
							case 5:
								String additionalClicks = cell.getStringCellValue();
								behavior.setAdditionalClickOnSteps(Integer.valueOf(additionalClicks));
								break;
							case 6:
								String searchForward = cell.getStringCellValue();
								behavior.setSearchForward(Integer.valueOf(searchForward));
								break;
							case 7:
								String searchBackward = cell.getStringCellValue();
								behavior.setSearchBackward(Integer.valueOf(searchBackward));
								break;
								
							}
						}
						
						BehaviorData.projectBehavior.put(projectName, behavior);
					}
					
				}
			}
			
		}
		
	}
}
