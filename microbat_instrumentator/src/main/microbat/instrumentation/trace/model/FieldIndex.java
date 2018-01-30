package microbat.instrumentation.trace.model;
import java.util.*;
 
  /** Every field gets assigned a unique int to identify it; this
   ** class holds (and updates) the mapping from field names to that int
   **/
public class FieldIndex {

  /** Returns a unique index for a field **/
  public static int getFieldIndex(String className, String fieldName) {
    return getIndex(fields, className + "." + fieldName);
  }
  /** Returns a unique index for a class **/
  public static int getClassIndex(String className) {
    return getIndex(classes, className);
  }
  public static void printIndexes(java.io.PrintStream ps) {
    ps.println("### Key to field and class names (represented as indexes above)");
    ps.println("- Classes:");
    printIndex(classes, ps);
    ps.println("- Fields:");
    printIndex(fields, ps);
  }
  private static int getIndex(Map<String,Integer> map, String key) {
    if (map.containsKey(key))
      return map.get(key);
    else { // Not there, so add it:
      int value = map.size();
      map.put(key, value);
      return value;
    }
  }
  public static void printIndex(Map<String,Integer> map, java.io.PrintStream ps) {
    for ( Map.Entry e : map.entrySet() )
      ps.printf("\t%8d = %s\n", e.getValue(), e.getKey());
  }
  private static Map<String,Integer> fields = new HashMap<String,Integer>();
  private static Map<String,Integer> classes = new HashMap<String,Integer>();
}


