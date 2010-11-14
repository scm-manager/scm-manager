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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.io.fs.FSEntry;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionNode;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockScope;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.handlers.IDAVResourceWalkHandler.CallType;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVResourceWalker {
    public static final int DAV_WALKTYPE_AUTH = 0x0001;
    public static final int DAV_WALKTYPE_NORMAL = 0x0002;
    public static final int DAV_WALKTYPE_LOCKNULL = 0x0004;

    private LinkedList myIfHeaders;
    private DAVResource myResource;
    private DAVLockInfoProvider myLockInfoProvider;
    private DAVLockScope myLockScope;
    private int myFlags;
    private int myWalkType;
    private FSRoot myRoot;
    
    public DAVResponse walk(DAVLockInfoProvider lockInfoProvider, DAVResource resource, LinkedList ifHeaders, int flags, DAVLockScope lockScope, 
            int walkType, IDAVResourceWalkHandler handler, DAVDepth depth) throws DAVException {
        myIfHeaders = ifHeaders;
        myLockInfoProvider = lockInfoProvider;
        myResource = resource.dup();
        myFlags = flags;
        myWalkType = walkType;
        myRoot = resource.getRoot();
        myLockScope = lockScope;
        return doWalk(handler, null, depth);
    }

    private DAVResponse doWalk(IDAVResourceWalkHandler handler, DAVResponse response, DAVDepth depth) throws DAVException {
        boolean isDir = myResource.isCollection();
        response = handler.handleResource(response, myResource, myLockInfoProvider, myIfHeaders, myFlags, myLockScope, 
                isDir ? CallType.COLLECTION : CallType.MEMBER);
        
        if (depth == DAVDepth.DEPTH_ZERO || !isDir) {
            return response;
        }
        
        if (myResource.isWorking()) {
            return response;
        }
        
        if (myResource.getType() != DAVResourceType.REGULAR) {
            throw new DAVException("Walking the resource hierarchy can only be done on 'regular' resources [at this time].", 
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED, 0);
        }
 
        //TODO: log here that we are listing a dir

        myResource.setExists(true);
        myResource.setCollection(false);
        FSRevisionNode node = null;
        Map children = null;
        try {
            node = myRoot.getRevisionNode(myResource.getResourceURI().getPath());
            children = node.getDirEntries(myRoot.getOwner());
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "could not fetch collection members", null);
        }
        
        for (Iterator childrenIter = children.keySet().iterator(); childrenIter.hasNext(); ) {
            String childName = (String) childrenIter.next();
            FSEntry childEntry = (FSEntry) children.get(childName);

            if ((myWalkType & DAV_WALKTYPE_AUTH) != 0) {
                //
            }
            
            String uriPath = myResource.getResourceURI().getURI();
            String reposPath = myResource.getResourceURI().getPath();
            
            myResource.getResourceURI().setURI(SVNPathUtil.append(uriPath, childName));
            myResource.getResourceURI().setPath(SVNPathUtil.append(reposPath, childName));
            
            if (childEntry.getType() == SVNNodeKind.FILE) {
                response = handler.handleResource(response, myResource, myLockInfoProvider, myIfHeaders, myFlags, myLockScope, CallType.MEMBER);
            } else {
                myResource.setCollection(true);
                try {
                    response = doWalk(handler, response, DAVDepth.decreaseDepth(depth));
                } catch (SVNException svne) {
                    throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                            "could not decrease depth", null);
                }
                myResource.setCollection(false);
            }
            
            myResource.getResourceURI().setURI(uriPath);
            myResource.getResourceURI().setPath(reposPath);
        }
        return response;
    }

}
