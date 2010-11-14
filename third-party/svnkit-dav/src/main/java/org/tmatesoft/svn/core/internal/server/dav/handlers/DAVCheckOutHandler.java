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
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.dav.http.HTTPHeader;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVCheckOutHandler extends ServletDAVHandler {

    private DAVCheckOutRequest myDAVRequest;
    
    public DAVCheckOutHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }

    public void execute() throws SVNException {
        long readLength = readInput(false);
     
        boolean applyToVSN = false;
        boolean isUnreserved = false;
        boolean createActivity = false;
        
        List activities = null;
        if (readLength > 0) {
            DAVCheckOutRequest davRequest = getCheckOutRequest();
            if (davRequest.isApplyToVersion()) {
                if (getRequestHeader(LABEL_HEADER) != null) {
                    response("DAV:apply-to-version cannot be used in conjunction with a Label header.", 
                            DAVServlet.getStatusLine(HttpServletResponse.SC_CONFLICT), HttpServletResponse.SC_CONFLICT);
                }
                applyToVSN = true;
            }
        
            isUnreserved = davRequest.isUnreserved();
            DAVElementProperty rootElement = davRequest.getRoot();
            DAVElementProperty activitySetElement = rootElement.getChild(DAVCheckOutRequest.ACTIVITY_SET);
            if (activitySetElement != null) {
                if (activitySetElement.hasChild(DAVCheckOutRequest.NEW)) {
                    createActivity = true;
                } else {
                    activities = new LinkedList();
                    List activitySetChildren = activitySetElement.getChildren();
                    for (Iterator activitySetIter = activitySetChildren.iterator(); activitySetIter.hasNext();) {
                        DAVElementProperty activitySetChild = (DAVElementProperty) activitySetIter.next();
                        if (activitySetChild.getName() == DAVElement.HREF) {
                            activities.add(activitySetChild.getFirstValue(true));
                        }
                    }
                    
                    if (activities.isEmpty()) {
                        throw new DAVException("Within the DAV:activity-set element, the DAV:new element must be used, or at least one DAV:href must be specified.", 
                                null, HttpServletResponse.SC_BAD_REQUEST, null, SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
                    }
                }
            }
        }
        
        DAVResource resource = getRequestedDAVResource(true, applyToVSN);
        if (!resource.exists()) {
            throw new DAVException(DAVServlet.getStatusLine(HttpServletResponse.SC_NOT_FOUND), null, HttpServletResponse.SC_NOT_FOUND, null, 
                    SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
        }
        
        if (resource.getResourceURI().getType() != DAVResourceType.REGULAR && 
                resource.getResourceURI().getType() != DAVResourceType.VERSION) {
            response("Cannot checkout this type of resource.", DAVServlet.getStatusLine(HttpServletResponse.SC_CONFLICT), 
                    HttpServletResponse.SC_CONFLICT);
        }
        
        if (!resource.isVersioned()) {
            response("Cannot checkout unversioned resource.", DAVServlet.getStatusLine(HttpServletResponse.SC_CONFLICT), 
                    HttpServletResponse.SC_CONFLICT);
        }
        
        if (resource.isWorking()) {
            response("The resource is already checked out to the workspace.", DAVServlet.getStatusLine(HttpServletResponse.SC_CONFLICT), 
                    HttpServletResponse.SC_CONFLICT);
        }

        DAVResource workingResource = null;
        try {
            workingResource = checkOut(resource, false, isUnreserved, createActivity, activities);
        } catch (DAVException dave) {
            throw new DAVException("Could not CHECKOUT resource {0}.", new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, 
                    HttpServletResponse.SC_CONFLICT, null, SVNLogType.NETWORK, Level.FINE, dave, null, null, 0, null);
        }
        
        setResponseHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
        
        if (workingResource == null) {
            setResponseHeader(HTTPHeader.CONTENT_LENGTH_HEADER, "0");
            return;
        }
            
        handleDAVCreated(workingResource.getResourceURI().getRequestURI(), "Checked-out resource", false);
    }

    protected DAVRequest getDAVRequest() {
        return getCheckOutRequest();
    }

    private DAVCheckOutRequest getCheckOutRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVCheckOutRequest();
        }
        return myDAVRequest;
    }

}
