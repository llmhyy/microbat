package microbat.views.providers;

import java.util.Optional;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.instrumentation.utils.MicrobatUtils;
import microbat.model.BreakPoint;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.ImageUI;
import debuginfo.NodeFeedbacksPair;

public class FeedbackNodePairLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ActionPathContentProvider.ContentWrapper) {
			ActionPathContentProvider.ContentWrapper node = (ActionPathContentProvider.ContentWrapper) element;			
			TraceNode traceNode = node.getNode().getNode();
			if (traceNode.hasChecked()) {
				if (!traceNode.isAllReadWrittenVarCorrect(true)) {
					return Settings.imageUI.getImage(ImageUI.WRONG_VALUE_MARK);
				} else if (traceNode.isWrongPathNode()) {
					return Settings.imageUI.getImage(ImageUI.WRONG_PATH_MARK);
				} else {
					return Settings.imageUI.getImage(ImageUI.CHECK_MARK);
				}
			} else {
				return Settings.imageUI.getImage(ImageUI.QUESTION_MARK);
			}
		}
		return null;
	}
	
	

	
	@Override
	public String getText(Object element) {
		// TODO Auto-generated method stub
		if (element instanceof ActionPathContentProvider.ContentWrapper) {
			ActionPathContentProvider.ContentWrapper node = (ActionPathContentProvider.ContentWrapper) element;			
			return MicroBatUtil.genPathMessage(node.getNode(), node.getIndex());					
		}
		return null;
	}
	
	
	
}
