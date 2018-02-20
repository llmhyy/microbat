package microbat.instrumentation.trace.testdata;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;

public class InvokeSample {
	

  public static void main(String[] args) {
      InvokeSample sc = new InvokeSample();
      sc.run();
      if (sc != null) {
    	  sc = null;
      }
  }

  private void run() { IExecutionTracer tracer = ExecutionTracer._getTracer(true, "InvokeSample", "run", 19, 22, "", 
		  "", new Object[1]);
      List<String> ls = new ArrayList<>();
      ls.add("Good Day");  tracer._hitInvoke(null, null, "methodname", null, "paramTypeSignsCode", "returnTypeSign", 17, "", "");

      ArrayList<String> als = new ArrayList<>();
      als.add("Dydh Da");
      
      System.out.println(join(";", "a", "b", "c"));
  }
  
  public static String join(String separator, Object... params) {
		return "unsupport";
	}
    
}