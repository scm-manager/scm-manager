/*
 * ====================================================================
 * Copyright (c) 2004-2009 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.server.dav.handlers;

import org.tmatesoft.svn.core.internal.io.dav.DAVElement;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class LivePropertySpecification {
    private DAVElement myPropertyName; 
    private boolean myIsWritable;
    private boolean myIsSVNSupported;

    public LivePropertySpecification(DAVElement propertyName, boolean isWritable, boolean isSVNSupported) {
        myIsWritable = isWritable;
        myPropertyName = propertyName;
        myIsSVNSupported = isSVNSupported;
    }
    
    public DAVElement getPropertyName() {
        return myPropertyName;
    }
    
    public boolean isWritable() {
        return myIsWritable;
    }

    public boolean isSVNSupported() {
        return myIsSVNSupported;
    }

}
