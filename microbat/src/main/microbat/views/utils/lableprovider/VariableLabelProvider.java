package microbat.views.utils.lableprovider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import microbat.model.value.VarValue;
import microbat.model.variable.VirtualVar;

public class VariableLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

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
		if(element instanceof VarValue varValue){
			switch(columnIndex){
			case 0: 
				String type = varValue.getType();
				if(type.contains(".")){
					type = type.substring(type.lastIndexOf(".")+1, type.length());
				}
				return type;
			case 1: 
				String name = varValue.getVarName();
				if(varValue.getVariable() instanceof VirtualVar){
					String methodName = name.substring(name.lastIndexOf(".")+1);
					name = "return from " + methodName + "()";
				}
				return name;
			case 2: 
				String value = varValue.getStringValue();
				return value;
			case 3:
				return String.format("%.2f", varValue.getComputationalCost());
			default:
				throw new IllegalArgumentException("Unhandled columnIndex: " + columnIndex);
			}
		}
		return null;
	}
	
}
