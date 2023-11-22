package microbat.views.styles;

import java.time.format.TextStyle;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class HighLightStyler extends Styler {

	@Override
	public void applyStyles(org.eclipse.swt.graphics.TextStyle textStyle) {
		// TODO Auto-generated method stub
		textStyle.background = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
	}
}
