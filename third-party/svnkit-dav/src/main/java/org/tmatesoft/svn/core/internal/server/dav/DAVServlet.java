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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVHandlerFactory;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVResponse;
import org.tmatesoft.svn.core.internal.server.dav.handlers.ServletDAVHandler;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVServlet extends HttpServlet {

    public static final String XML_CONTENT_TYPE = "text/xml; charset=\"utf-8\"";
    public static final String DAV_SVN_AUTOVERSIONING_ACTIVITY = "svn-autoversioning-activity";
    
    private static final Map OUR_STATUS_LINES = new SVNHashMap(); 
    private static final Map OUR_SHARED_CACHE = new SVNHashMap();
    
    static {
        OUR_STATUS_LINES.put(new Integer(100), "100 Continue");
        OUR_STATUS_LINES.put(new Integer(101), "101 Switching Protocols");
        OUR_STATUS_LINES.put(new Integer(102), "102 Processing");
        OUR_STATUS_LINES.put(new Integer(200), "200 OK");
        OUR_STATUS_LINES.put(new Integer(201), "201 Created");
        OUR_STATUS_LINES.put(new Integer(202), "202 Accepted");
        OUR_STATUS_LINES.put(new Integer(203), "203 Non-Authoritative Information");
        OUR_STATUS_LINES.put(new Integer(204), "204 No Content");
        OUR_STATUS_LINES.put(new Integer(205), "205 Reset Content");
        OUR_STATUS_LINES.put(new Integer(206), "206 Partial Content");
        OUR_STATUS_LINES.put(new Integer(207), "207 Multi-Status");
        OUR_STATUS_LINES.put(new Integer(300), "300 Multiple Choices");
        OUR_STATUS_LINES.put(new Integer(301), "301 Moved Permanently");
        OUR_STATUS_LINES.put(new Integer(302), "302 Found");
        OUR_STATUS_LINES.put(new Integer(303), "303 See Other");
        OUR_STATUS_LINES.put(new Integer(304), "304 Not Modified");
        OUR_STATUS_LINES.put(new Integer(305), "305 Use Proxy");
        OUR_STATUS_LINES.put(new Integer(306), "306 unused");
        OUR_STATUS_LINES.put(new Integer(307), "307 Temporary Redirect");
        OUR_STATUS_LINES.put(new Integer(400), "400 Bad Request");
        OUR_STATUS_LINES.put(new Integer(401), "401 Authorization Required");
        OUR_STATUS_LINES.put(new Integer(402), "402 Payment Required");
        OUR_STATUS_LINES.put(new Integer(403), "403 Forbidden");
        OUR_STATUS_LINES.put(new Integer(404), "404 Not Found");
        OUR_STATUS_LINES.put(new Integer(405), "405 Method Not Allowed");
        OUR_STATUS_LINES.put(new Integer(406), "406 Not Acceptable");
        OUR_STATUS_LINES.put(new Integer(407), "407 Proxy Authentication Required");
        OUR_STATUS_LINES.put(new Integer(408), "408 Request Time-out");
        OUR_STATUS_LINES.put(new Integer(409), "409 Conflict");
        OUR_STATUS_LINES.put(new Integer(410), "410 Gone");
        OUR_STATUS_LINES.put(new Integer(411), "411 Length Required");
        OUR_STATUS_LINES.put(new Integer(412), "412 Precondition Failed");
        OUR_STATUS_LINES.put(new Integer(413), "413 Request Entity Too Large");
        OUR_STATUS_LINES.put(new Integer(414), "414 Request-URI Too Large");
        OUR_STATUS_LINES.put(new Integer(415), "415 Unsupported Media Type");
        OUR_STATUS_LINES.put(new Integer(416), "416 Requested Range Not Satisfiable");
        OUR_STATUS_LINES.put(new Integer(417), "417 Expectation Failed");
        OUR_STATUS_LINES.put(new Integer(418), "418 unused");
        OUR_STATUS_LINES.put(new Integer(419), "419 unused");
        OUR_STATUS_LINES.put(new Integer(420), "420 unused");
        OUR_STATUS_LINES.put(new Integer(421), "421 unused");
        OUR_STATUS_LINES.put(new Integer(422), "422 Unprocessable Entity");
        OUR_STATUS_LINES.put(new Integer(423), "423 Locked");
        OUR_STATUS_LINES.put(new Integer(424), "424 Failed Dependency");
        OUR_STATUS_LINES.put(new Integer(425), "425 No code");
        OUR_STATUS_LINES.put(new Integer(426), "426 Upgrade Required");
        OUR_STATUS_LINES.put(new Integer(500), "500 Internal Server Error");
        OUR_STATUS_LINES.put(new Integer(501), "501 Method Not Implemented");
        OUR_STATUS_LINES.put(new Integer(502), "502 Bad Gateway");
        OUR_STATUS_LINES.put(new Integer(503), "503 Service Temporarily Unavailable");
        OUR_STATUS_LINES.put(new Integer(504), "504 Gateway Time-out");
        OUR_STATUS_LINES.put(new Integer(505), "505 HTTP Version Not Supported");
        OUR_STATUS_LINES.put(new Integer(506), "506 Variant Also Negotiates");
        OUR_STATUS_LINES.put(new Integer(507), "507 Insufficient Storage");
        OUR_STATUS_LINES.put(new Integer(508), "508 unused");
        OUR_STATUS_LINES.put(new Integer(509), "509 unused");
        OUR_STATUS_LINES.put(new Integer(510), "510 Not Extended");
    }
     
    private DAVConfig myDAVConfig;

    // scm-mangager change
    protected DAVConfig getDAVConfig() {
        return myDAVConfig;
    }

    public void init() {
        FSRepositoryFactory.setup();
        try {
            myDAVConfig = new DAVConfig(getServletConfig());
        } catch (SVNException e) {
            myDAVConfig = null;
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletDAVHandler handler = null;
        logRequest(request);//TODO: remove later
        try {
            DAVRepositoryManager repositoryManager = new DAVRepositoryManager(getDAVConfig(), request);
            handler = DAVHandlerFactory.createHandler(repositoryManager, request, response);
            handler.execute();
        } catch (DAVException de) {
            response.setContentType(XML_CONTENT_TYPE);
            handleError(de, response);
        } catch (SVNException svne) {
            StringWriter sw = new StringWriter();
            svne.printStackTrace(new PrintWriter(sw));
            String msg = sw.getBuffer().toString();
            SVNErrorCode errorCode = svne.getErrorMessage().getErrorCode();
            if (errorCode == SVNErrorCode.FS_NOT_DIRECTORY ||
                    errorCode == SVNErrorCode.FS_NOT_FOUND ||
                    errorCode == SVNErrorCode.RA_DAV_PATH_NOT_FOUND) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            } else if (errorCode == SVNErrorCode.NO_AUTH_FILE_PATH) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
            } else if (errorCode == SVNErrorCode.RA_NOT_AUTHORIZED) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
            } else {
                String errorBody = generateStandardizedErrorBody(errorCode.getCode(), null, null, svne.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(XML_CONTENT_TYPE);
                response.getWriter().print(errorBody);
            } 
        } catch (Throwable th) {
            StringWriter sw = new StringWriter();
            th.printStackTrace(new PrintWriter(sw));
            String msg = sw.getBuffer().toString();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        } finally {
            response.flushBuffer();
        }
    }

    private void logRequest(HttpServletRequest request) {
        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append('\n');
        logBuffer.append("request.getAuthType(): " + request.getAuthType());
        logBuffer.append('\n');
        logBuffer.append("request.getCharacterEncoding(): " + request.getCharacterEncoding());
        logBuffer.append('\n');
        logBuffer.append("request.getContentType(): " + request.getContentType());
        logBuffer.append('\n');
        logBuffer.append("request.getContextPath(): " + request.getContextPath());
        logBuffer.append('\n');
        logBuffer.append("request.getContentLength(): " + request.getContentLength());
        logBuffer.append('\n');
        logBuffer.append("request.getMethod(): " + request.getMethod());
        logBuffer.append('\n');
        logBuffer.append("request.getPathInfo(): " + request.getPathInfo());
        logBuffer.append('\n');
        logBuffer.append("request.getPathTranslated(): " + request.getPathTranslated());
        logBuffer.append('\n');
        logBuffer.append("request.getQueryString(): " + request.getQueryString());
        logBuffer.append('\n');
        logBuffer.append("request.getRemoteAddr(): " + request.getRemoteAddr());
        logBuffer.append('\n');
        logBuffer.append("request.getRemoteHost(): " + request.getRemoteHost());
        logBuffer.append('\n');
        logBuffer.append("request.getRemoteUser(): " + request.getRemoteUser());
        logBuffer.append('\n');
        logBuffer.append("request.getRequestURI(): " + request.getRequestURI());
        logBuffer.append('\n');
        logBuffer.append("request.getServerName(): " + request.getServerName());
        logBuffer.append('\n');
        logBuffer.append("request.getServerPort(): " + request.getServerPort());
        logBuffer.append('\n');
        logBuffer.append("request.getServletPath(): " + request.getServletPath());
        logBuffer.append('\n');
        logBuffer.append("request.getRequestURL(): " + request.getRequestURL());
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, logBuffer.toString());
    }
    
    public static void handleError(DAVException error, HttpServletResponse servletResponse) throws IOException {
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, error);
        DAVResponse response = error.getResponse();
        if (response == null) {
            DAVException stackErr = error;
            while (stackErr != null && stackErr.getTagName() == null) {
                stackErr = stackErr.getPreviousException();
            }
            
            if (stackErr != null && stackErr.getTagName() != null) {
                servletResponse.setContentType(XML_CONTENT_TYPE);
                servletResponse.setStatus(stackErr.getResponseCode());

                StringBuffer errorMessageBuffer = new StringBuffer();
                SVNXMLUtil.addXMLHeader(errorMessageBuffer);
                errorMessageBuffer.append('\n');
                errorMessageBuffer.append("<D:error xmlns:D=\"DAV:\"");
                
                if (stackErr.getMessage() != null) {
                    errorMessageBuffer.append(" xmlns:m=\"http://apache.org/dav/xmlns\"");
                }
                
                if (stackErr.getNameSpace() != null) {
                    errorMessageBuffer.append(" xmlns:C=\"");
                    errorMessageBuffer.append(stackErr.getNameSpace());
                    errorMessageBuffer.append("\">\n<C:");
                    errorMessageBuffer.append(stackErr.getTagName());
                    errorMessageBuffer.append("/>");
                } else {
                    errorMessageBuffer.append(">\n<D:");
                    errorMessageBuffer.append(stackErr.getTagName());
                    errorMessageBuffer.append("/>");
                }
                
                if (stackErr.getMessage() != null) {
                    errorMessageBuffer.append("<m:human-readable errcode=\"" );
                    errorMessageBuffer.append(stackErr.getErrorID());
                    errorMessageBuffer.append("\">\n");
                    errorMessageBuffer.append(SVNEncodingUtil.xmlEncodeCDATA(stackErr.getMessage()));
                    errorMessageBuffer.append('\n');
                    errorMessageBuffer.append("</m:human-readable>\n");
                }
                errorMessageBuffer.append("</D:error>\n");
                servletResponse.getWriter().print(errorMessageBuffer.toString());
                SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, errorMessageBuffer.toString());
                return;
            }
            servletResponse.setStatus(error.getResponseCode());
            return;
        }
        
        DAVXMLUtil.sendMultiStatus(response, servletResponse, error.getResponseCode(), null);
    }
    
    private String generateStandardizedErrorBody(int errorID, String namespace, String tagName, String description) {
        StringBuffer xmlBuffer = new StringBuffer();
        SVNXMLUtil.addXMLHeader(xmlBuffer);
        Collection namespaces = new ArrayList();
        namespaces.add(DAVElement.DAV_NAMESPACE);
        namespaces.add(DAVElement.SVN_APACHE_PROPERTY_NAMESPACE);
        if (namespace != null) {
            namespaces.add(namespace);
        }
        SVNXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVXMLUtil.SVN_DAV_ERROR_TAG, namespaces, 
                SVNXMLUtil.PREFIX_MAP, xmlBuffer);
        String prefix = (String) SVNXMLUtil.PREFIX_MAP.get(namespace);
        if (prefix != null) {
            prefix = SVNXMLUtil.DAV_NAMESPACE_PREFIX;
        }
        if (tagName != null && tagName.length() > 0) {
            SVNXMLUtil.openXMLTag(prefix, tagName, SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, xmlBuffer);
        }

        SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_APACHE_PROPERTY_PREFIX, "human-readable", SVNXMLUtil.XML_STYLE_NORMAL, "errcode", 
                String.valueOf(errorID), xmlBuffer);
        xmlBuffer.append(SVNEncodingUtil.xmlEncodeCDATA(description));
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_APACHE_PROPERTY_PREFIX, "human-readable", xmlBuffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVXMLUtil.SVN_DAV_ERROR_TAG, xmlBuffer);
        return xmlBuffer.toString();
    }
    
    public static String getStatusLine(int statusCode) {
        return (String) OUR_STATUS_LINES.get(new Integer(statusCode));
    }
    
    public static String getSharedActivity() {
        synchronized (OUR_SHARED_CACHE) {
            return (String) OUR_SHARED_CACHE.get(DAV_SVN_AUTOVERSIONING_ACTIVITY);    
        }
    }
    
    public static void setSharedActivity(String sharedActivity) {
        synchronized (OUR_SHARED_CACHE) {
            OUR_SHARED_CACHE.put(DAV_SVN_AUTOVERSIONING_ACTIVITY, sharedActivity);
        }
    }
    
    public static boolean isHTTPServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
}