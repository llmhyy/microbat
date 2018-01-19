/*
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
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


package microbat.codeanalysis.runtime.jpda.tty;

import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.event.WatchpointEvent;

interface EventNotifier {
    void vmStartEvent(VMStartEvent e);
    void vmDeathEvent(VMDeathEvent e);
    void vmDisconnectEvent(VMDisconnectEvent e);

    void threadStartEvent(ThreadStartEvent e);
    void threadDeathEvent(ThreadDeathEvent e);

    void classPrepareEvent(ClassPrepareEvent e);
    void classUnloadEvent(ClassUnloadEvent e);

    void breakpointEvent(BreakpointEvent e);
    void fieldWatchEvent(WatchpointEvent e);
    void stepEvent(StepEvent e);
    void exceptionEvent(ExceptionEvent e);
    void methodEntryEvent(MethodEntryEvent e);
    boolean methodExitEvent(MethodExitEvent e);

    void vmInterrupted();
    void receivedEvent(Event event);
}
