package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;

public class DefiniteConstraint extends Constraint {

	public DefiniteConstraint(BitRepresentation variablesIncluded, int conclusionIndex, boolean isTrue) {
		super(variablesIncluded, conclusionIndex, isTrue ? Configs.HIGH : Configs.LOW);
	}
	
	public DefiniteConstraint(BitRepresentation variablesIncluded, int conclusionIndex) {
		this(variablesIncluded, conclusionIndex, true);
	}
}
