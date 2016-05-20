package microbat.views;

import org.eclipse.jface.text.source.Annotation;

public class ReferenceAnnotation extends Annotation {
	public static String ANNOTATION_TYPE = "microbat.specification.step";
	
	public ReferenceAnnotation(boolean isPersistent, String text){
		super(ReferenceAnnotation.ANNOTATION_TYPE, isPersistent, text);
	}
}
