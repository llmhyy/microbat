/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.parser.descriptor.proxy;

import mutation.parser.ClassDescriptor;
import mutation.parser.descriptor.IClassDescriptor;

/**
 * @author LLT
 *
 */
public class ClassDescriptorProxy implements IClassDescriptor {
	private ClassDescriptor desc;

	@Override
	public String getQuantifiedName() {
		return null;
	}
	
	
}
