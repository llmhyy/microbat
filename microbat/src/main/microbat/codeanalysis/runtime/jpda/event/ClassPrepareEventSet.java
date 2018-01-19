/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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


package microbat.codeanalysis.runtime.jpda.event;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventSet;

public class ClassPrepareEventSet extends AbstractEventSet {

    private static final long serialVersionUID = 5958493423581010491L;

    ClassPrepareEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }

    /**
     * Returns the thread in which this event has occurred.
     *
     * @return a {@link ThreadReference} which mirrors the event's thread in
     * the target VM.
     */
    public ThreadReference getThread() {
        return ((ClassPrepareEvent)oneEvent).thread();
    }


    /**
     * Returns the reference type for which this event was generated.
     *
     * @return a {@link ReferenceType} which mirrors the class, interface, or
     * array which has been linked.
     */
    public ReferenceType getReferenceType() {
        return ((ClassPrepareEvent)oneEvent).referenceType();
    }

    @Override
    public void notify(JDIListener listener) {
        listener.classPrepare(this);
    }
}
