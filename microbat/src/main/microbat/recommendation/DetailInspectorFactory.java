package microbat.recommendation;

import microbat.util.Settings;

public class DetailInspectorFactory {
	public static DetailInspector createInspector(){
		if(Settings.isApplyAdvancedInspector){
			return new AdvancedDetailInspector();
		}
		else{
			return new SimpleDetailInspector();
		}
	}
}
