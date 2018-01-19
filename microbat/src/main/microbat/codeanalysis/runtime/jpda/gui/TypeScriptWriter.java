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

import java.io.IOException;
import java.io.Writer;

public class TypeScriptWriter extends Writer {

    TypeScript script;

    public TypeScriptWriter(TypeScript script) {
        this.script = script;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        script.append(String.valueOf(cbuf, off, len));
    }

    @Override
    public void flush() {
        script.flush();
    }

    @Override
    public void close() {
        script.flush();
    }
}
