package microbat.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

public class StepImageProvider implements IAnnotationImageProvider {

	public StepImageProvider() {
	}

	public Image getManagedImage(Annotation annotation) {
		Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED);
		return image;
	}

	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return null;
	}

}
