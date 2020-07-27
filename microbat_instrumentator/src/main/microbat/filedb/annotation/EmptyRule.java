package microbat.filedb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author LLT 
 * if this field is empty, the object is consider empty, no field
 *         needs to be stored.
 */
@Target({ElementType.METHOD, ElementType.FIELD}) 
public @interface EmptyRule {

}
