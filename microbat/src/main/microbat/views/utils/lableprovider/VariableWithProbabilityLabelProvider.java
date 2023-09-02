package microbat.views.utils.lableprovider;

import microbat.model.value.VarValue;

public class VariableWithProbabilityLabelProvider extends VariableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof VarValue varValue) {
			switch (columnIndex) {
			case 0:
			case 1:
			case 2:
				return super.getColumnText(element, columnIndex);
			case 3:
				return String.format("%.4f", varValue.getProbability());
			case 4:
				return String.format("%.2f", varValue.getComputationalCost());
			default:
				throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			}
		}
		return null;
	}
}
