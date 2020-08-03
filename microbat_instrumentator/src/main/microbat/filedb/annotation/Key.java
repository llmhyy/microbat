package microbat.filedb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author LLT
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Key {

}
