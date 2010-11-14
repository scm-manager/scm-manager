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


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSLocationsFinder;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.ISVNLocationSegmentHandler;
import org.tmatesoft.svn.core.io.SVNLocationSegment;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVGetLocationSegmentsHandler extends DAVReportHandler implements ISVNLocationSegmentHandler {

    private static final String GET_LOCATION_SEGMENTS_REPORT = "get-location-segments-report";
    private static final String LOCATION_SEGMENT_TAG = "location-segment";
    private static final String RANGE_START_ATTR = "range-start";
    private static final String RANGE_END_ATTR = "range-end";
    
    private DAVGetLocationSegmentsRequest myDAVRequest;
    private boolean myIsOpenerSent;
    private DAVReportHandler myCommonReportHandler;
    
    protected DAVGetLocationSegmentsHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response, 
            DAVReportHandler commonReportHandler) {
        super(repositoryManager, request, response);
        myCommonReportHandler = commonReportHandler;
    }

    public void execute() throws SVNException {
        myCommonReportHandler.checkSVNNamespace(null);

        DAVResource resource = getRequestedDAVResource(false, false); 
        setDAVResource(resource);
        
        DAVGetLocationSegmentsRequest request = getLocationSegmentsRequest();
        
        String path = null; 
        long startRev = -1;
        long endRev = -1;
        long pegRev = -1;

        DAVElementProperty rootElement = request.getRootElement();
        List children = rootElement.getChildren();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            DAVElementProperty childElement = (DAVElementProperty) iterator.next();
            DAVElement childElementName = childElement.getName();
            if (!DAVElement.SVN_NAMESPACE.equals(childElementName.getNamespace())) {
                continue;
            }
            
            if (childElementName == DAVElement.PATH) {
                path = childElement.getFirstValue(false);
                DAVPathUtil.testCanonical(path);
                String resourcePath = resource.getResourceURI().getPath();
                path = SVNPathUtil.append(resourcePath, path);
            } else if (childElementName == DAVElement.START_REVISION) {
                try {
                    startRev = Long.parseLong(childElement.getFirstValue(true));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (childElementName == DAVElement.END_REVISION) {
                try {
                    endRev = Long.parseLong(childElement.getFirstValue(true));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (childElementName == DAVElement.PEG_REVISION) {
                try {
                    pegRev = Long.parseLong(childElement.getFirstValue(true));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            }
        }
    
        if (path == null) {
            throw new DAVException("Not all parameters passed.", HttpServletResponse.SC_BAD_REQUEST, SVNLogType.NETWORK, DAVXMLUtil.SVN_DAV_ERROR_TAG, 
                    DAVElement.SVN_DAV_ERROR_NAMESPACE);
        }
        
        if (SVNRevision.isValidRevisionNumber(startRev) && SVNRevision.isValidRevisionNumber(endRev) && 
                endRev > startRev) {
            throw new DAVException("End revision must not be younger than start revision", HttpServletResponse.SC_BAD_REQUEST, SVNLogType.NETWORK, 
                    DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE);
        }
        
        if (SVNRevision.isValidRevisionNumber(pegRev) && SVNRevision.isValidRevisionNumber(startRev) && 
                startRev > pegRev) {
            throw new DAVException("Start revision must not be younger than peg revision", HttpServletResponse.SC_BAD_REQUEST, SVNLogType.NETWORK, 
                    DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE);
        }
        
        FSLocationsFinder locationsFinder = new FSLocationsFinder(getDAVResource().getFSFS());
        locationsFinder.getNodeLocationSegments(path, pegRev, startRev, endRev, this);
        try {
            maybeSendOpener(); 
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Error beginning REPORT reponse", null);
        }
        try {
            writeXMLFooter(GET_LOCATION_SEGMENTS_REPORT);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Error ending REPORT reponse", null);
        }
    }

    public void handleLocationSegment(SVNLocationSegment locationSegment) throws SVNException {
        maybeSendOpener();
        Map attrs = new HashMap();

        String path = locationSegment.getPath();
        if (path != null) {
            String quotedPath = SVNEncodingUtil.xmlEncodeCDATA(path, true);
            attrs.put(PATH_ATTR, quotedPath);
            attrs.put(RANGE_START_ATTR, String.valueOf(locationSegment.getStartRevision()));
            attrs.put(RANGE_END_ATTR, String.valueOf(locationSegment.getEndRevision()));
        } else {
            attrs.put(RANGE_START_ATTR, String.valueOf(locationSegment.getStartRevision()));
            attrs.put(RANGE_END_ATTR, String.valueOf(locationSegment.getEndRevision()));
        }
        StringBuffer buffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, LOCATION_SEGMENT_TAG, SVNXMLUtil.XML_STYLE_SELF_CLOSING, attrs, null);
        write(buffer);
    }

    protected DAVRequest getDAVRequest() {
        return getLocationSegmentsRequest();
    }

    private DAVGetLocationSegmentsRequest getLocationSegmentsRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVGetLocationSegmentsRequest();
        }
        return myDAVRequest;
    }

    private void maybeSendOpener() throws SVNException {
        if (!myIsOpenerSent) {
            writeXMLHeader(GET_LOCATION_SEGMENTS_REPORT);
            myIsOpenerSent = true;
        }
    }
}
