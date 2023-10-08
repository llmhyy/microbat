package microbat.views.utils.lableprovider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import microbat.model.trace.TraceNode;

public class ControlDominatorLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof TraceNode node) {
			final TraceNode controlDom = node.getControlDominator();
			switch (columnIndex) {
			case 0: 
				return controlDom == null ? "-" : String.valueOf(controlDom.getOrder());
			case 1:
				return controlDom == null ? "-" : String.format("%.4f", controlDom.getConditionResult().getProbability());
			case 2:
				return controlDom == null ? "-" : String.format("%.2f", controlDom.getConditionResult().getSuspiciousness());
			default:
				throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			}
		}
		return null;
	}

}
