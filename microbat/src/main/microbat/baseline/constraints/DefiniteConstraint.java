package microbat.baseline.constraints;

import java.util.BitSet;

import microbat.baseline.Configs;

public class DefiniteConstraint extends Constraint {

	public DefiniteConstraint(BitSet variablesIncluded, int conclusionIndex) {
		super(variablesIncluded, conclusionIndex, Configs.HIGH);
	}
}
