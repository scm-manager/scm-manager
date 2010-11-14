/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.server.dav;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVResourceType {
    public static final DAVResourceType REGULAR = new DAVResourceType("regular");
    public static final DAVResourceType WORKING = new DAVResourceType("working");
    public static final DAVResourceType VERSION = new DAVResourceType("version");
    public static final DAVResourceType PRIVATE = new DAVResourceType("private");
    public static final DAVResourceType ACTIVITY = new DAVResourceType("activity");
    public static final DAVResourceType HISTORY = new DAVResourceType("history");
    public static final DAVResourceType WORKSPACE = new DAVResourceType("workspace");

    private String myName;

    private DAVResourceType(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
