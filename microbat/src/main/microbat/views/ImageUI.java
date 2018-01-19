package microbat.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import microbat.Activator;

public class ImageUI {

	public static final String CHECK_MARK = "icons/check_mark.png";
	public static final String WRONG_VALUE_MARK = "icons/cross_mark.png";
	public static final String WRONG_PATH_MARK = "icons/path_mark.png";
	public static final String QUESTION_MARK = "icons/question_mark.png";
	public static final String UNDO_MARK = "icons/undo.png";

//	private Image checkMarkImage;
//	private Image buggyMarkImage;
//	private Image questionMarkImage;
	
	private Map<String, Image> imageMap = new HashMap<>();
	
	public ImageUI(){
		
	}
	
	public Image getImage(String icon){
		Image image = imageMap.get(icon);
		if(image == null){
			ImageDescriptor imageDesc = Activator.getDefault().getImageRegistry().getDescriptor(icon);
			image = imageDesc.createImage();
			imageMap.put(icon, image);
		}
		
		return image;
	}
	
	
	
}
