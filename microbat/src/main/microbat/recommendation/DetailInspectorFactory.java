package microbat.recommendation;

import microbat.util.Settings;

public class DetailInspectorFactory {
	public static DetailInspector createInspector(){
		if(Settings.isApplyAdvancedInspector){
			return new DataOmissionInspector();
		}
		else{
			return new SimpleDetailInspector();
		}
	}
}
