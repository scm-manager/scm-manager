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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceKind;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;

import org.xml.sax.Attributes;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVOptionsHandler extends ServletDAVHandler {

    private static final DAVElement OPTIONS = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "options");
    private static final DAVElement ACTIVITY_COLLECTION_SET = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "activity-collection-set");
    private static final DAVElement SUPPORTED_METHOD_SET = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "supported-method-set");
    private static final DAVElement SUPPORTED_METHOD = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "supported-method");
    private static final DAVElement SUPPORTED_LIVE_PROPERTY_SET = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "supported-live-property-set");
    private static final DAVElement SUPPORTED_LIVE_PROPERTY = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "supported-live-property");
    private static final DAVElement SUPPORTED_REPORT_SET = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "supported-report-set");
    private static final DAVElement SUPPORTED_REPORT = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "supported-report");

    private static final String DAV_HEADER = "DAV";
    private static final String DAV_LEVEL = "1,2";
    private static final String VERSION_OPTIONS_FIRST_PART = "version-control,checkout,working-resource";
    private static final String VERSION_OPTIONS_SECOND_PART = "merge,baseline,activity,version-controlled-collection";
    private static final String MS_AUTHOR_VIA_HEADER = "MS-Author-Via";
    private static final String ALLOW_HEADER = "Allow";

    private DAVOptionsRequest myDAVRequest;

    public DAVOptionsHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response) {
        super(connector, request, response);
    }

    protected DAVRequest getDAVRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVOptionsRequest();
        }
        return myDAVRequest;
    }

    private DAVOptionsRequest getOptionsRequest() {
        return (DAVOptionsRequest) getDAVRequest();
    }

    public void execute() throws SVNException {
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "in execute() of DAVOptiondsHandler");
        
        readInput(false);
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "read input");
        
        DAVResource resource = getRequestedDAVResource(false, false);
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "got resource");

        Collection supportedMethods = getSupportedMethods(resource);
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "got supported methods");

        StringBuffer body = new StringBuffer();
        generateOptionsResponse(resource, supportedMethods, body);
        String responseBody = body.toString();

        try {
            setResponseContentLength(responseBody.getBytes(UTF8_ENCODING).length);
        } catch (UnsupportedEncodingException e) {
            setResponseContentLength(responseBody.getBytes().length);
        }

        setDefaultResponseHeaders();
        setResponseHeaders(supportedMethods);
        setResponseContentType(DEFAULT_XML_CONTENT_TYPE);
        setResponseStatus(HttpServletResponse.SC_OK);

        try {
            getResponseWriter().write(responseBody);
        } catch (IOException e) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, e), e, SVNLogType.NETWORK);
        }
    }

    protected void startElement(DAVElement parent, DAVElement element, Attributes attrs) throws SVNException {
        if (parent == null && element != OPTIONS) {
            invalidXML();
        } else if (parent == OPTIONS && element == ACTIVITY_COLLECTION_SET) {
            getOptionsRequest().setActivitySetRequest(true);
        } else if (parent == SUPPORTED_METHOD_SET && element == SUPPORTED_METHOD) {
            String requestedMethodName = attrs.getValue(DAVElement.DAV_NAMESPACE, NAME_ATTR);
            if (requestedMethodName == null || getOptionsRequest().getRequestedMethods() == null) {
                invalidXML();
            }
            getOptionsRequest().getRequestedMethods().add(requestedMethodName);
        } else if (parent == SUPPORTED_REPORT_SET && element == SUPPORTED_REPORT) {
            String requestedReportName = attrs.getValue(DAVElement.DAV_NAMESPACE, NAME_ATTR);
            String requestedReportNamespace = attrs.getValue(DAVElement.DAV_NAMESPACE, NAMESPACE_ATTR);
            if (requestedReportName == null || getOptionsRequest().getRequestedReports() == null) {
                invalidXML();
            }
            if (requestedReportNamespace == null) {
                requestedReportNamespace = DAVElement.SVN_NAMESPACE;
            }
            getOptionsRequest().getRequestedReports().add(DAVElement.getElement(requestedReportNamespace, requestedReportName));
        } else if (parent == SUPPORTED_LIVE_PROPERTY_SET && element == SUPPORTED_LIVE_PROPERTY) {
            String requestedLivePropertyName = attrs.getValue(DAVElement.DAV_NAMESPACE, NAME_ATTR);
            String requestedLivePropertyNamespace = attrs.getValue(DAVElement.DAV_NAMESPACE, NAMESPACE_ATTR);
            if (requestedLivePropertyName == null || getOptionsRequest().getRequestedLiveProperties() == null) {
                invalidXML();
            }
            if (requestedLivePropertyNamespace == null) {
                requestedLivePropertyNamespace = DAVElement.DAV_NAMESPACE;
            }
            getOptionsRequest().getRequestedLiveProperties().add(DAVElement.getElement(requestedLivePropertyNamespace, requestedLivePropertyName));
        }
    }

    protected void endElement(DAVElement parent, DAVElement element, StringBuffer cdata) throws SVNException {
    }

    private static Collection getSupportedMethods(DAVResource resource) throws SVNException {
        //TODO: when work with locks will be implemented, we need to check resource state: LOCK_NULL, EXIST, NULL.
        Collection supportedMethods = new ArrayList();
        supportedMethods.add(DAVHandlerFactory.METHOD_OPTIONS);
        if (resource.exists()) {
            supportedMethods.add(DAVHandlerFactory.METHOD_GET);
            supportedMethods.add(DAVHandlerFactory.METHOD_HEAD);
            supportedMethods.add(DAVHandlerFactory.METHOD_POST);
            supportedMethods.add(DAVHandlerFactory.METHOD_DELETE);
            supportedMethods.add(DAVHandlerFactory.METHOD_TRACE);
            supportedMethods.add(DAVHandlerFactory.METHOD_PROPFIND);
            supportedMethods.add(DAVHandlerFactory.METHOD_PROPPATCH);
            supportedMethods.add(DAVHandlerFactory.METHOD_MOVE);
            supportedMethods.add(DAVHandlerFactory.METHOD_COPY);
            supportedMethods.add(DAVHandlerFactory.METHOD_LOCK);
            supportedMethods.add(DAVHandlerFactory.METHOD_UNLOCK);
            if (!resource.isCollection()) {
                supportedMethods.add(DAVHandlerFactory.METHOD_PUT);
            }
        }
        //TODO: native svn checks if resource is auto checked out.
        if (resource.getResourceURI().getType() == DAVResourceType.ACTIVITY && !resource.exists()) {
            supportedMethods.add(DAVHandlerFactory.METHOD_MKACTIVITY);
        } else if (resource.getResourceURI().isWorking()) {
            supportedMethods.add(DAVHandlerFactory.METHOD_CHECKIN);
        } else {
            supportedMethods.add(DAVHandlerFactory.METHOD_CHECKOUT);
        }
        return supportedMethods;
    }

    private void setResponseHeaders(Collection supportedMethods) {
        //MSFT Web Folders chokes if length of DAV header value > 63 characters! (c) Subversion.
        setResponseHeader(DAV_HEADER, DAV_LEVEL);
        addResponseHeader(DAV_HEADER, VERSION_OPTIONS_FIRST_PART);
        addResponseHeader(DAV_HEADER, VERSION_OPTIONS_SECOND_PART);
        addResponseHeader(DAV_HEADER, DAVElement.DEPTH_OPTION);
        addResponseHeader(DAV_HEADER, DAVElement.LOG_REVPROPS_OPTION);
        addResponseHeader(DAV_HEADER, DAVElement.PARTIAL_REPLAY_OPTION);
        addResponseHeader(DAV_HEADER, DAVElement.MERGE_INFO_OPTION);
        setResponseHeader(MS_AUTHOR_VIA_HEADER, DAV_HEADER);
        setResponseHeader(ALLOW_HEADER, generateAllowHeaderValue(supportedMethods));
    }

    private String generateAllowHeaderValue(Collection supportedMethods) {
        StringBuffer allowHeaderBuffer = new StringBuffer();
        for (Iterator iterator = supportedMethods.iterator(); iterator.hasNext();) {
            allowHeaderBuffer.append(iterator.next());
            allowHeaderBuffer.append(iterator.hasNext() ? "," : "");
        }
        return allowHeaderBuffer.toString();
    }

    private void generateOptionsResponse(DAVResource resource, Collection supportedMethods, StringBuffer xmlBuffer) throws SVNException {
        if (!getOptionsRequest().isEmpty()) {
            SVNXMLUtil.addXMLHeader(xmlBuffer);
            DAVXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "options-response", null, xmlBuffer, false);
            if (getOptionsRequest().isActivitySetRequest()) {
                generateActivityCollectionSet(resource, xmlBuffer);
            } else if (getOptionsRequest().isSupportedLivePropertiesRequest()) {
                generateSupportedLivePropertySet(resource, xmlBuffer);
            } else if (getOptionsRequest().isSupportedMethodsRequest()) {
                generateSupportedMethodSet(supportedMethods, xmlBuffer);
            } else if (getOptionsRequest().isSupportedReportsRequest()) {
                generateSupportedReportSet(resource, xmlBuffer);
            }
            SVNXMLUtil.addXMLFooter(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "options-response", xmlBuffer);
        }
    }

    private void generateActivityCollectionSet(DAVResource resource, StringBuffer xmlBuffer) {
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "activity-collection-set", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        String uri = DAVPathUtil.buildURI(resource.getResourceURI().getContext(), DAVResourceKind.ACT_COLLECTION, 0, null, false);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "href", uri, xmlBuffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "activity-collection-set", xmlBuffer);
    }

    private void generateSupportedLivePropertySet(DAVResource resource, StringBuffer xmlBuffer) throws SVNException {
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-live-property-set", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        Collection supportedLiveProperties = getSupportedLiveProperties(resource, null);
        generateSupportedElementSet(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-live-property", supportedLiveProperties, getOptionsRequest().getRequestedLiveProperties(), xmlBuffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-live-property-set", xmlBuffer);
    }

    private void generateSupportedMethodSet(Collection supportedMethods, StringBuffer xmlBuffer) {
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-method-set", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        generateSupportedElementSet(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-method", supportedMethods, getOptionsRequest().getRequestedMethods(), xmlBuffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-method-set", xmlBuffer);
    }

    private void generateSupportedReportSet(DAVResource resource, StringBuffer xmlBuffer) {
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-report-set", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        if (resource.getResourceURI().getType() == DAVResourceType.REGULAR) {
            generateSupportedElementSet(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-report", REPORT_ELEMENTS, getOptionsRequest().getRequestedReports(), xmlBuffer);
            SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "supported-report-set", xmlBuffer);
        }
    }

    private void generateSupportedElementSet(String prefix, String tagName, Collection supportedElements, Collection requestedElements, StringBuffer xmlBuffer) {
        for (Iterator iterator = supportedElements.iterator(); iterator.hasNext();) {
            Object item = iterator.next();
            if (requestedElements.isEmpty() || requestedElements.contains(item)) {
                Map attrs = new SVNHashMap();
                if (item instanceof DAVElement) {
                    DAVElement currentElement = (DAVElement) item;
                    attrs.put(NAME_ATTR, currentElement.getNamespace());
                    attrs.put(NAMESPACE_ATTR, currentElement.getName());
                } else if (item instanceof String) {
                    String currentName = (String) item;
                    attrs.put(NAME_ATTR, currentName);
                }
                SVNXMLUtil.openXMLTag(prefix, tagName, SVNXMLUtil.XML_STYLE_SELF_CLOSING, attrs, xmlBuffer);
            }
        }
    }
}

