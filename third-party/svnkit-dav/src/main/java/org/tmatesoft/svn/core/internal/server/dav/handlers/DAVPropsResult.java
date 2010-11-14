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
package org.tmatesoft.svn.core.internal.server.dav.handlers;

import java.util.Collection;
import java.util.LinkedList;


/**
 * @version 1.1.2
 * @author  TMate Software Ltd.
 */
public class DAVPropsResult {
    private StringBuffer myPropStats;
    private Collection myNamespaces;
    
    public void addNamespace(String namespace) {
        if (myNamespaces == null) {
            myNamespaces = new LinkedList();
        }
        myNamespaces.add(namespace);
    }
    
    public void addPropStatsText(String text) {
        if (myPropStats == null) {
            myPropStats = new StringBuffer();
        }
        myPropStats.append(text);    
    }
    
    public Collection getNamespaces() {
        return myNamespaces;
    }
    
    public String getPropStatsText() {
        if (myPropStats != null) {
            return myPropStats.toString();
        }
        return null;
    }
    
}
