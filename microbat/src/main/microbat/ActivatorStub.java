package microbat;

import org.eclipse.jface.preference.IPreferenceStore;

public class ActivatorStub extends Activator {
	private final IPreferenceStore preferenceStore;
	
	public ActivatorStub(IPreferenceStore preferenceStore) {
		super();
		this.preferenceStore = preferenceStore;
	}
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
}
