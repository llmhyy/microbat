package microbat.breakpoint;

import static org.junit.Assert.fail;

import java.util.List;

import microbat.model.BreakPoint;
import microbat.model.variable.Variable;

public class BreakPointChecker {

	private List<BreakPoint> runningStatements = null;
	private List<BreakPoint> runningStatements0 = null;
	
	public void setUp(){
		
	}
	
	//@Test
	public void testBreakPoint() {
		for(BreakPoint breakpoint: runningStatements){
			for(BreakPoint breakpoint0: runningStatements0){
				if(breakpoint.getLineNumber() == breakpoint0.getLineNumber()){
					
					if(breakpoint.getReadVariables().size() == breakpoint0.getReadVariables().size()){
						for(Variable var: breakpoint.getReadVariables()){
							boolean isFound = false;
							for(Variable var0: breakpoint0.getReadVariables()){
								if(var.getName().equals(var0.getName())){
									isFound = true;
									break;
								}
							}
							
							if(!isFound){
								fail();
							}
						}
					}
					else{
						fail();
					}
					
					if(breakpoint.getWrittenVariables().size() == breakpoint0.getWrittenVariables().size()){
						for(Variable var: breakpoint.getWrittenVariables()){
							boolean isFound = false;
							for(Variable var0: breakpoint0.getWrittenVariables()){
								if(var.getName().equals(var0.getName())){
									isFound = true;
									break;
								}
							}
							
							if(!isFound){
								fail();
							}
						}
					}
					else{
						fail();
					}
					break;
				}
			}
			
		}
	}

}
