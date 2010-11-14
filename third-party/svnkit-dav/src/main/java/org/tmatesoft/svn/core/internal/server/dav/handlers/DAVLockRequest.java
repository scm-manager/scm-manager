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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockRecType;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockScope;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockType;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVLockRequest extends DAVRequest {
    private static final DAVElement LOCK_INFO = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "lockinfo");

    public DAVLock parseLockInfo(DAVLockHandler handler, DAVResource resource, List namespaces) throws DAVException {
        DAVDepth depth = null;
        try {
            depth = handler.getRequestDepth(DAVDepth.DEPTH_INFINITY);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_BAD_REQUEST, null, null);
        }
        
        if (depth != DAVDepth.DEPTH_ONE && depth != DAVDepth.DEPTH_ZERO) {
            throw new DAVException("An invalid Depth header was specified.", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        Date timeout = handler.getTimeout();
        String lockToken = null;
        
        try {
            lockToken = FSRepositoryUtil.generateLockToken();
        } catch (SVNException e) {
            DAVException first = DAVException.convertError(e.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate a lock token.", null);
            DAVException next = new DAVException("Could not parse the lockinfo due to an internal problem creating a lock structure.", first.getResponseCode(), first, 0);
            throw next;
        }
        
        DAVElementProperty rootElement = null;
        try {
            rootElement = getRoot();
        } catch (SVNException e) {
            throw DAVException.convertError(e.getErrorMessage(), HttpServletResponse.SC_BAD_REQUEST, 
                    "Could not parse the lockinfo, malformed xml request", null);
        } 

        String owner = null;
        DAVLockScope lockScope = DAVLockScope.UNKNOWN;
        DAVLockType lockType = DAVLockType.UNKNOWN; 
        for (Iterator childrenIter = rootElement.getChildren().iterator(); childrenIter.hasNext();) {
            DAVElementProperty childElement = (DAVElementProperty) childrenIter.next();
            DAVElement childElementName = childElement.getName();
            if (childElementName == DAVElement.LOCK_TYPE && childElement.getChildren() != null && lockType == DAVLockType.UNKNOWN) {
                DAVElementProperty writeChild = childElement.getChild(DAVElement.WRITE);
                if (writeChild != null) {
                    lockType = DAVLockType.WRITE;
                    continue;
                }
            }
            if (childElementName == DAVElement.LOCK_SCOPE && childElement.getChildren() != null && lockScope == DAVLockScope.UNKNOWN) {
                if (childElement.getChild(DAVElement.EXCLUSIVE) != null) {
                    lockScope = DAVLockScope.EXCLUSIVE;
                } else if (childElement.getChild(DAVElement.SHARED) != null) {
                    lockScope = DAVLockScope.SHARED;
                }
                if (lockScope != DAVLockScope.UNKNOWN) {
                    continue;
                }
            }
            
            if (childElementName == DAVElement.LOCK_OWNER) {
                //TODO: maybe make this recursive for all possible subelements   
                StringBuffer buffer = new StringBuffer();
                String namespace = childElementName.getNamespace();
                buffer.append("<");
                if (namespace == null || "".equals(namespace)) {
                    buffer.append(childElementName.getName());
                } else {
                    buffer.append(SVNXMLUtil.PREFIX_MAP.get(namespace));
                    buffer.append(":");
                    buffer.append(childElementName.getName());
                }
                
                Map attributes = childElement.getAttributes();
                if (attributes != null) {
                    for (Iterator attrsIter = attributes.keySet().iterator(); attrsIter.hasNext();) {
                        String attrName = (String) attrsIter.next();
                        String attrValue = (String) attributes.get(attrName);
                        buffer.append(" ");
                        buffer.append(attrName);
                        buffer.append("=\"");
                        buffer.append(attrValue);
                        buffer.append("\"");
                    }
                }
                
                for (Iterator namespacesIter = namespaces.iterator(); namespacesIter.hasNext();) {
                    String nextNamespace = (String) namespacesIter.next();
                    buffer.append(" xmlns:");
                    buffer.append(SVNXMLUtil.PREFIX_MAP.get(nextNamespace));
                    buffer.append("=\"");
                    buffer.append(nextNamespace);
                    buffer.append("\"");
                }
                
                if (childElement.isEmpty()) {
                    buffer.append(" />");
                } else {
                    buffer.append(">");
                    buffer.append(SVNEncodingUtil.xmlEncodeCDATA(childElement.getFirstValue(false), false));
                    buffer.append("</");
                    if (namespace == null || "".equals(namespace)) {
                        buffer.append(childElementName.getName());
                    } else {
                        buffer.append(SVNXMLUtil.PREFIX_MAP.get(namespace));
                        buffer.append(":");
                        buffer.append(childElementName.getName());
                    }
                    buffer.append(">");
                }
                
                owner = buffer.toString();
                continue;
            }
            
            throw new DAVException("The server cannot satisfy the LOCK request due to an unknown XML element (\"{0}\") within the DAV:lockinfo element.", 
                    new Object[] { childElementName.getName() }, HttpServletResponse.SC_PRECONDITION_FAILED, 0);
        }
        
        return new DAVLock(resource.getUserName(), depth, resource.exists(), lockToken, owner, DAVLockRecType.DIRECT, lockScope, lockType, timeout);

    }
    
    protected void init() throws SVNException {
        if (getRoot().getName() != LOCK_INFO) {
            invalidXMLRoot();
        }
    }

    protected DAVElementProperty getRoot() throws SVNException {
        if (getRootElement() == null) {
            invalidXMLRoot();
        }
        return getRootElement();
    }

    protected void invalidXMLRoot() throws SVNException {
        throw new DAVException("The request body contains an unexpected XML root element.", null, HttpServletResponse.SC_BAD_REQUEST, 
                null, SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
    }

}
