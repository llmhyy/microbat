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
@Target(ElementType.TYPE)
public @interface RecordType {

	/**
	 * (Optional) The name of the table.
	 * <p>
	 * Defaults to the entity name.
	 */
	String name() default "";
}
