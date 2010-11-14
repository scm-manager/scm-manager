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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;
import org.xml.sax.Attributes;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVElementProperty {
    private ArrayList myValues;
    private Map myAttributes;
    private List myChildren;
    private DAVElement myElementName;
    private DAVElementProperty myParent;
    private LivePropertySpecification myLivePropertySpec;
    
    public DAVElementProperty(DAVElement elementName, DAVElementProperty parent) {
        myElementName = elementName;
        myParent = parent;
    }
    
    public DAVElementProperty getParent() {
        return myParent;
    }
    
    public DAVElement getName() {
        return myElementName;
    }
    
    public void setElementName(DAVElement element) {
        myElementName = element;
    }
    
    public boolean hasChild(DAVElement element) {
        return getChild(element) != null;
    }
    
    public DAVElementProperty getChild(DAVElement element) {
        if (myChildren != null) {
            for (Iterator childrenIter = myChildren.iterator(); childrenIter.hasNext();) {
                DAVElementProperty nextChild = (DAVElementProperty) childrenIter.next();
                if (element == nextChild.getName()) {
                    return nextChild;
                }
            }
        }
        return null;
    }
    
    public Map getAttributes() {
        return myAttributes;
    }
    
    public void addValue(String cdata) throws SVNException {
        if (myChildren != null) {
            invalidXML();
        } else if (myValues == null) {
            myValues = new ArrayList();
        }
        myValues.add(cdata);
    }

    public String getFirstValue(boolean trim) {
        if (myValues != null) {
            String value = (String) myValues.get(0); 
            if (trim && value != null) {
                value = value.trim();
            }
            return value;
        }
        return null;
    }

    public Collection getValues() {
        return myValues;
    }

    public String getAttributeValue(String name) {
        if (myAttributes != null) {
            return (String) myAttributes.get(name);
        }
        return null;
    }

    public void setAttributes(Attributes attributes) {
        myAttributes = getAttributesMap(attributes);
    }

    public List getChildren() {
        return myChildren;
    }

    public boolean isEmpty() {
        return (myChildren == null || myChildren.isEmpty()) && (myValues == null || myValues.isEmpty());
    }
    
    protected DAVElementProperty addChild(DAVElement element, Attributes attrs) throws SVNException {
        if (myValues != null) {
            invalidXML();
        } else if (myChildren == null) {
            myChildren = new LinkedList();
        }
        
        DAVElementProperty child = new DAVElementProperty(element, this);
        myChildren.add(child);
        child.setAttributes(attrs);
        return child;
    }

    private Map getAttributesMap(Attributes attrs) {
        Map attributes = null;
        if (attrs.getLength() != 0) {
            attributes = new SVNHashMap();
            for (int i = 0; i < attrs.getLength(); i++) {
                attributes.put(attrs.getLocalName(i), attrs.getValue(i));
            }
        }
        return attributes;
    }

    private void invalidXML() throws SVNException {
        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.XML_MALFORMED, "Malformed XML"), SVNLogType.NETWORK);
    }

}
