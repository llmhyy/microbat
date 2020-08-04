package microbat.filedb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LLT
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD}) 
public @interface Attribute {
	
	 /**
     * (Optional) The name of the column. Defaults to 
     * the property or field name.
     */
    String name() default "";
    
    /**
     * (Optional) the storage order
     */
    int order() default -1;
    
    /** 
     * (Optional) Whether the association should be lazily 
     * loaded or must be eagerly fetched. The EAGER
     * strategy is a requirement on the persistence provider runtime that 
     * the associated entity must be eagerly fetched. The LAZY 
     * strategy is a hint to the persistence provider runtime.
     */
    FetchType fetch() default FetchType.EAGER;
}
