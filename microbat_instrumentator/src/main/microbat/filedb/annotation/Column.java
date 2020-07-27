package microbat.filedb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author LLT
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD}) 
public @interface Column {
	
	 /**
     * (Optional) The name of the column. Defaults to 
     * the property or field name.
     */
    String name() default "";
    
    /**
     * (Optional) the storage order
     */
    int order() default -1;
}
