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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVLockInfoProvider;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public abstract class DAVResourceHelper {

    private static final Map ourResourceHelpers = new HashMap();
    static {
        registerHelper(DAVResourceType.WORKING, new DAVWorkingResourceHelper());
        registerHelper(DAVResourceType.REGULAR, new DAVRegularResourceHelper());
        registerHelper(DAVResourceType.ACTIVITY, new DAVActivityResourceHelper());
        registerHelper(DAVResourceType.HISTORY, new DAVHistoryResourceHelper());
        registerHelper(DAVResourceType.PRIVATE, new DAVPrivateResourceHelper());
        registerHelper(DAVResourceType.VERSION, new DAVVersionResourceHelper());
    }
    
    protected abstract void prepare(DAVResource resource) throws DAVException;

    protected abstract DAVResource getParentResource(DAVResource resource) throws DAVException;
    
    public static void prepareResource(DAVResource resource) throws DAVException {
        DAVResourceURI resourceURI = resource.getResourceURI();
        DAVResourceType resourceType = resourceURI.getType();
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "resource type is " + resourceType.toString());
        DAVResourceHelper helperImpl = getHelper(resourceType);
        helperImpl.prepare(resource);
    }
    
    public static DAVResource createParentResource(DAVResource resource) throws DAVException {
        DAVResourceURI resourceURI = resource.getResourceURI();
        DAVResourceType resourceType = resourceURI.getType();
        DAVResourceHelper helperImpl = getHelper(resourceType);
        return helperImpl.getParentResource(resource);
    }
    
    public static DAVResource getDirectResource(DAVLockInfoProvider lockProvider, String lockToken, DAVResource resource) throws DAVException {
        while (resource != null) {
            DAVLock lock = lockProvider.findLock(resource, lockToken);
            if (lock == null) {
                throw new DAVException("The specified locktoken does not correspond to an existing lock on this resource.", 
                        HttpServletResponse.SC_BAD_REQUEST, 0);
            }
            
            if (lock.getRecType() == DAVLockRecType.DIRECT) {
                return resource;
            }
            
            resource = createParentResource(resource);
        }
        throw new DAVException("The lock database is corrupt. A direct lock could not be found for the corresponding indirect lock on this resource.", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 0);
    }
    
    public static void throwIllegalGetParentResourceError(DAVResource resource) throws DAVException {
        DAVResourceURI uri = resource.getResourceURI();
        throw new DAVException("getParentResource() was called for {0} (type {1})", new Object[] { uri.getRequestURI(), 
                uri.getType() }, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 0); 
    }

    public static void convertWorkingToRegular(DAVResource resource) throws DAVException {
        DAVResourceURI uri = resource.getResourceURI();
        uri.setType(DAVResourceType.REGULAR);
        
        resource.setWorking(false);
        String path = null;
        FSFS fsfs = resource.getFSFS();
        if (!SVNRevision.isValidRevisionNumber(resource.getRevision())) {
            long rev = SVNRepository.INVALID_REVISION;
            
            try {
                rev = resource.getLatestRevision();
            } catch (SVNException e) {
                throw DAVException.convertError(e.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not determine youngest rev.", null);
            }
            
            resource.setRevision(rev);
            path = uri.getPath();
        } else {
            path = DAVPathUtil.buildURI(uri.getContext(), DAVResourceKind.BASELINE_COLL, resource.getRevision(), uri.getPath(), false);
        }
        
        path = SVNEncodingUtil.uriEncode(path);
        uri.setURI(path);
        try {
            resource.setRoot(fsfs.createRevisionRoot(resource.getRevision()));
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Could not open revision root.", null);
        }
    }

    private static DAVResourceHelper getHelper(DAVResourceType resourceType) throws DAVException {
        DAVResourceHelper helperImpl = (DAVResourceHelper) ourResourceHelpers.get(resourceType);
        if (helperImpl == null) {
            throw new DAVException("DESIGN FAILURE: unknown resource type", null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, 
                    SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
        }
        return helperImpl;
    }
    
    protected synchronized static void registerHelper(DAVResourceType resourceType, DAVResourceHelper factoryImpl) {
        ourResourceHelpers.put(resourceType, factoryImpl);
    }
}
