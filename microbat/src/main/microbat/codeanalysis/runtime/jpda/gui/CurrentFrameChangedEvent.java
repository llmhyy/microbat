/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */


package microbat.codeanalysis.runtime.jpda.gui;

import java.util.EventObject;

import com.sun.jdi.ThreadReference;

import microbat.codeanalysis.runtime.jpda.bdi.ThreadInfo;

public class CurrentFrameChangedEvent extends EventObject {

    private static final long serialVersionUID = 4214479486546762179L;
    private ThreadInfo tinfo;
    private int index;
    private boolean invalidate;

    public CurrentFrameChangedEvent(Object source, ThreadInfo tinfo,
                                    int index, boolean invalidate) {
        super(source);
        this.tinfo = tinfo;
        this.index = index;
        this.invalidate = invalidate;
    }

    public ThreadReference getThread() {
        return tinfo == null? null : tinfo.thread();
    }

    public ThreadInfo getThreadInfo() {
        return tinfo;
    }

    public int getIndex() {
        return index;
    }

    public boolean getInvalidate() {
        return invalidate;
    }
}
