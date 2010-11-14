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

import java.util.LinkedList;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVIFHeader {
    private String myURI;
    private LinkedList myStateList;
    private boolean myIsDummyHeader;
    
    public DAVIFHeader(String uri) {
        this(uri, false);
    }

    public DAVIFHeader(String uri, boolean isDummy) {
        myURI = uri;
        myStateList = new LinkedList();
        myIsDummyHeader = isDummy;
    }
    
    public void addIFState(DAVIFState ifState) {
        myStateList.addFirst(ifState);
    }

    public boolean isDummyHeader() {
        return myIsDummyHeader;
    }

    public String getURI() {
        return myURI;
    }

    public LinkedList getStateList() {
        return myStateList;
    }

}
