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

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNMergeInfo;
import org.tmatesoft.svn.core.SVNMergeRangeList;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVMergeInfoHandler extends DAVReportHandler {
    
    private static final String MERGEINFO_REPORT = "mergeinfo-report";
    private DAVMergeInfoRequest myDAVRequest;
    private DAVReportHandler myCommonReportHandler;

    public DAVMergeInfoHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response, 
            DAVReportHandler commonReportHandler) {
        super(repositoryManager, request, response);
        myCommonReportHandler = commonReportHandler;
    }

    protected DAVRequest getDAVRequest() {
        return getMergeInfoRequest();
    }

    private DAVMergeInfoRequest getMergeInfoRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVMergeInfoRequest();
        }
        return myDAVRequest;
    }

    public void execute() throws SVNException {
        myCommonReportHandler.checkSVNNamespace(null);

        setDAVResource(getRequestedDAVResource(false, false));

        String responseBody = generateResponseBody();
        try {
            setResponseContentLength(responseBody.getBytes(UTF8_ENCODING).length);
        } catch (UnsupportedEncodingException e) {
        }

        write(responseBody);
    }

    private String generateResponseBody() throws SVNException {
        StringBuffer xmlBuffer = new StringBuffer();
        addXMLHeader(xmlBuffer, MERGEINFO_REPORT);

        for (int i = 0; i < getMergeInfoRequest().getTargetPaths().length; i++) {
            String currentPath = getMergeInfoRequest().getTargetPaths()[i];
            DAVPathUtil.testCanonical(currentPath);
            if (currentPath.length() == 0 || currentPath.charAt(0) != '/') {
                getMergeInfoRequest().getTargetPaths()[i] = SVNPathUtil.append(getDAVResource().getResourceURI().getPath(), currentPath);
            }
        }

        //TODO: fixme - add includeDescendants parameter
        Map mergeInfoMap = getDAVResource().getRepository().getMergeInfo(getMergeInfoRequest().getTargetPaths(), 
                getMergeInfoRequest().getRevision(), getMergeInfoRequest().getInherit(), false);
        if (mergeInfoMap != null && !mergeInfoMap.isEmpty()) {
            for (Iterator iterator = mergeInfoMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String path = (String) entry.getKey();
                SVNMergeInfo mergeInfo = (SVNMergeInfo) entry.getValue();
                addMergeInfo(path, mergeInfo, xmlBuffer);
            }
        }

        addXMLFooter(xmlBuffer, MERGEINFO_REPORT);
        return xmlBuffer.toString();
    }

    private void addMergeInfo(String path, SVNMergeInfo mergeInfo, StringBuffer xmlBuffer) {
        SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "mergeinfo-item", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "mergeinfo-path", path, xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "mergeinfo-info", addSourcePathes(mergeInfo), xmlBuffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "mergeinfo-item", xmlBuffer);
    }

    private String addSourcePathes(SVNMergeInfo mergeInfo) {
        StringBuffer result = new StringBuffer();
        for (Iterator iterator = mergeInfo.getMergeSourcesToMergeLists().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String sourcePath = (String) entry.getKey();
            SVNMergeRangeList rangeList = (SVNMergeRangeList) entry.getValue();
            result.append(sourcePath);
            result.append(":");
            result.append(rangeList.toString());
            if (iterator.hasNext()) {
                result.append('\n');
            }
        }
        return result.toString();
    }
}
