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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVPropfindRequest extends DAVRequest {

    private static final DAVElement PROPFIND = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "propfind");
    private static final DAVElement PROPNAME = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "propname");
    private static final DAVElement ALLPROP = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "allprop");

    public DAVPropfindRequest() {
        super();
    }

    protected void init() throws SVNException {
        if (getRoot().getName() != PROPFIND) {
            invalidXML();
        }
    }

    public boolean isAllPropRequest() throws SVNException {
        return getRoot().hasChild(ALLPROP);
    }

    public boolean isPropNameRequest() throws SVNException {
        return getRoot().hasChild(PROPNAME);
    }

    public boolean isPropRequest() throws SVNException {
        return getRoot().hasChild(DAVElement.PROP);
    }

    public Collection getPropertyElements() throws SVNException {
        DAVElementProperty propElement = getRoot().getChild(DAVElement.PROP);
        List props = new LinkedList();
        List children = propElement.getChildren();
        if (children != null) {
            for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
                DAVElementProperty child = (DAVElementProperty) childrenIter.next();
                props.add(child.getName());
            }
        }
        return props;
    }

    private DAVElementProperty getRoot() throws SVNException {
        if (getRootElement() == null) {
            invalidXML();
        }
        return getRootElement();
    }
}
