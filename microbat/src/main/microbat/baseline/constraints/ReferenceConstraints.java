package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;

public class ReferenceConstraints extends Constraint {
	public ReferenceConstraints(BitRepresentation variablesIncluded, int conclusionIndex, int n) {
		super(variablesIncluded, conclusionIndex, tau(n));
	}
	
	private static double tau(int n) {
		return 0.5 + 0.5 * (2 * Configs.HIGH - 1) * (1/n);
	}
}
