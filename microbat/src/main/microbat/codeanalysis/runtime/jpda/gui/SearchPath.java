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


package microbat.codeanalysis.runtime.jpda.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class SearchPath {

    private String pathString;

    private String[] pathArray;

    public SearchPath(String searchPath) {
        //### Should check searchpath for well-formedness.
        StringTokenizer st = new StringTokenizer(searchPath, File.pathSeparator);
        List<String> dlist = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            dlist.add(st.nextToken());
        }
        pathString = searchPath;
        pathArray = dlist.toArray(new String[dlist.size()]);
    }

    public boolean isEmpty() {
        return (pathArray.length == 0);
    }

    public String asString() {
        return pathString;
    }

    public String[] asArray() {
        return pathArray.clone();
    }

    public File resolve(String relativeFileName) {
        for (String element : pathArray) {
            File path = new File(element, relativeFileName);
            if (path.exists()) {
                return path;
            }
        }
        return null;
    }

    //### return List?

    public String[] children(String relativeDirName, FilenameFilter filter) {
        // If a file appears at the same relative path
        // with respect to multiple entries on the classpath,
        // the one corresponding to the earliest entry on the
        // classpath is retained.  This is the one that will be
        // found if we later do a 'resolve'.
        SortedSet<String> s = new TreeSet<String>();  // sorted, no duplicates
        for (String element : pathArray) {
            File path = new File(element, relativeDirName);
            if (path.exists()) {
                String[] childArray = path.list(filter);
                if (childArray != null) {
                    for (int j = 0; j < childArray.length; j++) {
                        if (!s.contains(childArray[j])) {
                            s.add(childArray[j]);
                        }
                    }
                }
            }
        }
        return s.toArray(new String[s.size()]);
    }

}
