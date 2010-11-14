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

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

import org.xml.sax.Attributes;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public abstract class DAVRequest {

    protected static final String NAME_ATTR = "name";
    protected static final String NAMESPACE_ATTR = "namespace";

    private DAVElementProperty myRootElementProperty;
    private DAVElementProperty myCurrentElement;

    public DAVElementProperty getRootElement() {
        return myRootElementProperty;
    }

    protected String getRootElementAttributeValue(String name) {
        if (myRootElementProperty != null) {
            return myRootElementProperty.getAttributeValue(name);
        }
        return null;
    }

    public void startElement(DAVElement parent, DAVElement element, Attributes attrs) throws SVNException {
        if (parent == null) {
            myCurrentElement = new DAVElementProperty(element, null);
            myCurrentElement.setAttributes(attrs);
            myRootElementProperty = myCurrentElement;
        } else {
            myCurrentElement = myCurrentElement.addChild(element, attrs);
        }
    }

    public void endElement(DAVElement parent, DAVElement element, StringBuffer cdata) throws SVNException {
        if (myCurrentElement == null || element != myCurrentElement.getName()) {
            invalidXML();
        }
        
        if (cdata != null) {
            myCurrentElement.addValue(cdata.toString());
        }
        
        myCurrentElement = myCurrentElement.getParent();
        if (myCurrentElement == null && parent != null) {
            invalidXML();
        }
    }

    protected abstract void init() throws SVNException;

    protected void invalidXML() throws SVNException {
        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.XML_MALFORMED, "Malformed XML"), SVNLogType.NETWORK);
    }

    protected void invalidXML(DAVElement element) throws SVNException {
        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.XML_MALFORMED, "\"The request's ''{0}'' element is malformed; there is a problem with the client.", element.getName()), SVNLogType.NETWORK);
    }

    protected void assertNullCData(DAVElement element, DAVElementProperty property) throws SVNException {
        if (property.getValues() == null) {
            invalidXML(element);
        }
    }

}
