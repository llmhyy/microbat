package microbat.filedb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author LLT
 *
 */
@Target(ElementType.TYPE)
public @interface Table {

	/**
	 * (Optional) The name of the table.
	 * <p>
	 * Defaults to the entity name.
	 */
	String name() default "";
}
