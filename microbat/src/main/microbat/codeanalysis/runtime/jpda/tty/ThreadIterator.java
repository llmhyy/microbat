/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Iterator;
import java.util.List;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

class ThreadIterator implements Iterator<ThreadReference> {
    Iterator<ThreadReference> it = null;
    ThreadGroupIterator tgi;

    ThreadIterator(ThreadGroupReference tg) {
        tgi = new ThreadGroupIterator(tg);
    }

    ThreadIterator(List<ThreadGroupReference> tgl) {
        tgi = new ThreadGroupIterator(tgl);
    }

    ThreadIterator() {
        tgi = new ThreadGroupIterator();
    }

    @Override
    public boolean hasNext() {
        while (it == null || !it.hasNext()) {
            if (!tgi.hasNext()) {
                return false; // no more
            }
            it = tgi.nextThreadGroup().threads().iterator();
        }
        return true;
    }

    @Override
    public ThreadReference next() {
        return it.next();
    }

    public ThreadReference nextThread() {
        return next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
