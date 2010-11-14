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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNBase64;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.util.SVNHashSet;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.util.SVNLogType;
import org.xml.sax.Attributes;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVReportHandler extends ServletDAVHandler {

    protected static final Set REPORT_NAMESPACES = new SVNHashSet();

    protected static final String PATH_ATTR = "path";
    protected static final String REVISION_ATTR = "rev";
    protected static final String COPYFROM_PATH_ATTR = "copyfrom-path";
    protected static final String COPYFROM_REVISION_ATTR = "copyfrom-rev";
    protected static final String TXDELTA_ATTR = "txdelta";
    protected static final String CHECKSUM_ATTR = "checksum";
    protected static final String DELETE_ATTR = "del";
    protected static final String LOCK_TOKEN_ATTR = "lock-token";
    protected static final String LINKPATH_ATTR = "linkpath";
    protected static final String DEPTH_ATTR = "depth";
    protected static final String START_EMPTY_ATTR = "start-empty";
    protected static final String SEND_ALL_ATTR = "send-all";
    protected static final String BASE_CHECKSUM_ATTR = "base-checksum";
    protected static final String BC_URL_ATTR = "bc-url";

    private DAVRepositoryManager myRepositoryManager;
    private HttpServletRequest myRequest;
    private HttpServletResponse myResponse;

    private DAVReportHandler myReportHandler;
    private DAVResource myDAVResource;
    private OutputStream myDiffWindowWriter;

    private boolean myWriteTextDeltaHeader = true;
    private boolean mySVNDiffVersion = false;
    private boolean myIsUnknownReport;

    static {
        REPORT_NAMESPACES.add(DAVElement.SVN_NAMESPACE);
    }

    protected DAVReportHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response) {
        super(connector, request, response);
        myRepositoryManager = connector;
        myRequest = request;
        myResponse = response;
    }

    protected List getNamespaces() {
        return super.getNamespaces();
    }
    
    protected DAVRequest getDAVRequest() {
        return getReportHandler().getDAVRequest();
    }

    private DAVReportHandler getReportHandler() {
        return myReportHandler;
    }

    private void setReportHandler(DAVReportHandler reportHandler) {
        myReportHandler = reportHandler;
    }

    protected DAVResource getDAVResource() {
        return myDAVResource;
    }

    protected void setDAVResource(DAVResource DAVResource) {
        myDAVResource = DAVResource;
    }

    protected void checkSVNNamespace(String errorMessage) throws DAVException {
        errorMessage = errorMessage == null ? "The request does not contain the 'svn:' namespace, so it is not going to have certain required elements." : errorMessage; 
        List namespaces = getNamespaces();
        if (!namespaces.contains(DAVElement.SVN_NAMESPACE)) {
            throw new DAVException(errorMessage, HttpServletResponse.SC_BAD_REQUEST, SVNLogType.NETWORK, DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE);
        }
    }

    public boolean doCompress() {
        return mySVNDiffVersion;
    }

    public void setSVNDiffVersion(boolean SVNDiffVersion) {
        mySVNDiffVersion = SVNDiffVersion;
    }

    private boolean isWriteTextDeltaHeader() {
        return myWriteTextDeltaHeader;
    }

    protected void setWriteTextDeltaHeader(boolean writeTextDeltaHeader) {
        myWriteTextDeltaHeader = writeTextDeltaHeader;
    }

    protected void startElement(DAVElement parent, DAVElement element, Attributes attrs) throws SVNException {
        if (parent == null) {
            initReportHandler(element);
        }
        getReportHandler().handleAttributes(parent, element, attrs);
    }

    protected void handleAttributes(DAVElement parent, DAVElement element, Attributes attrs) throws SVNException {
        getDAVRequest().startElement(parent, element, attrs);
    }

    protected void endElement(DAVElement parent, DAVElement element, StringBuffer cdata) throws SVNException {
        getReportHandler().handleCData(parent, element, cdata);
    }

    protected void handleCData(DAVElement parent, DAVElement element, StringBuffer cdata) throws SVNException {
        getDAVRequest().endElement(parent, element, cdata);
    }

    public void execute() throws SVNException {
        long read = readInput(false);
        if (myIsUnknownReport) {
            throw new DAVException("The requested report is unknown.", null, HttpServletResponse.SC_NOT_IMPLEMENTED, null, SVNLogType.DEFAULT, Level.FINE, 
                    null, DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE, SVNErrorCode.UNSUPPORTED_FEATURE.getCode(), null);
        }
        
        if (read == 0) {
            throw new DAVException("The request body must specify a report.", HttpServletResponse.SC_BAD_REQUEST, SVNLogType.NETWORK);
        }

        setDefaultResponseHeaders();
        setResponseContentType(DEFAULT_XML_CONTENT_TYPE);
        setResponseStatus(HttpServletResponse.SC_OK);

        getReportHandler().execute();
    }

    private void initReportHandler(DAVElement rootElement) {
        myIsUnknownReport = false;
        if (rootElement == DATED_REVISIONS_REPORT) {
            setReportHandler(new DAVDatedRevisionHandler(myRepositoryManager, myRequest, myResponse));
        } else if (rootElement == FILE_REVISIONS_REPORT) {
            setReportHandler(new DAVFileRevisionsHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == GET_LOCATIONS) {
            setReportHandler(new DAVGetLocationsHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == LOG_REPORT) {
            setReportHandler(new DAVLogHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == MERGEINFO_REPORT) {
            setReportHandler(new DAVMergeInfoHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == GET_LOCKS_REPORT) {
            setReportHandler(new DAVGetLocksHandler(myRepositoryManager, myRequest, myResponse));
        } else if (rootElement == REPLAY_REPORT) {
            setReportHandler(new DAVReplayHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == UPDATE_REPORT) {
            setReportHandler(new DAVUpdateHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == GET_LOCATION_SEGMENTS) {
            setReportHandler(new DAVGetLocationSegmentsHandler(myRepositoryManager, myRequest, myResponse, this));
        } else if (rootElement == GET_DELETED_REVISION_REPORT) {
            setReportHandler(new DAVGetDeletedRevisionHandler(myRepositoryManager, myRequest, myResponse, this));
        } else {
            myIsUnknownReport = true;
            setReportHandler(new DumpReportHandler(myRepositoryManager, myRequest, myResponse));
        }
    }

    protected void write(String string) throws SVNException {
        try {
            getResponseWriter().write(string);
        } catch (IOException e) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, e), e, SVNLogType.NETWORK);
        }
    }

    protected void write(StringBuffer stringBuffer) throws SVNException {
        write(stringBuffer.toString());
    }

    protected void writeXMLHeader(String tagName) throws SVNException {
        StringBuffer xmlBuffer = new StringBuffer();
        addXMLHeader(xmlBuffer, tagName);
        write(xmlBuffer);
    }

    protected void writeXMLFooter(String tagName) throws SVNException {
        StringBuffer xmlBuffer = new StringBuffer();
        addXMLFooter(xmlBuffer, tagName);
        write(xmlBuffer);
    }

    protected void addXMLHeader(StringBuffer xmlBuffer, String tagName) {
        SVNXMLUtil.addXMLHeader(xmlBuffer);
        DAVElementProperty rootElement = getDAVRequest().getRootElement();
        tagName = tagName == null ? rootElement.getName().getName() : tagName;
        DAVXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, REPORT_NAMESPACES, 
                xmlBuffer, false);
    }

    protected void addXMLFooter(StringBuffer xmlBuffer, String tagName) {
        DAVElementProperty rootElement = getDAVRequest().getRootElement();
        tagName = tagName == null ? rootElement.getName().getName() : tagName;
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, xmlBuffer);
    }

    protected void writeTextDeltaChunk(SVNDiffWindow diffWindow) throws SVNException {
        if (myDiffWindowWriter == null) {
            myDiffWindowWriter = new DAVBase64OutputStream(getResponseWriter());
        }
        try {
            diffWindow.writeTo(myDiffWindowWriter, isWriteTextDeltaHeader(), doCompress());
        } catch (IOException e) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, e), e, SVNLogType.NETWORK);
        } finally {
            setWriteTextDeltaHeader(false);
        }
    }

    protected void textDeltaChunkEnd() throws SVNException {
        if (myDiffWindowWriter != null) {
            try {
                myDiffWindowWriter.flush();
            } catch (IOException e) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, e), e, SVNLogType.NETWORK);
            }
        }
        myDiffWindowWriter = null;
    }

    protected void writePropertyTag(String tagName, String propertyName, SVNPropertyValue propertyValue) throws SVNException {
        StringBuffer xmlBuffer;
        if (propertyValue == null){
            xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, SVNXMLUtil.XML_STYLE_SELF_CLOSING, NAME_ATTR, propertyName, null);
            write(xmlBuffer);
            return;
        }
        String value = propertyValue.getString();
        boolean isXMLSafe = true;
        if (propertyValue.isBinary()){
            CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            try {
                value = decoder.decode(ByteBuffer.wrap(propertyValue.getBytes())).toString();
            } catch (CharacterCodingException e) {
                isXMLSafe = false;
            }
        }
        if (value != null){
            isXMLSafe = SVNEncodingUtil.isXMLSafe(value);
        }
        if (!isXMLSafe){
            byte[] buffer = null;
            if (value != null){
                try {
                    buffer = value.getBytes(UTF8_ENCODING);
                } catch (UnsupportedEncodingException e) {
                    buffer = value.getBytes();
                }
            } else {
                buffer = propertyValue.getBytes();
            }
            value = SVNBase64.byteArrayToBase64(buffer);
            
            Map attrs = new SVNHashMap();
            attrs.put(NAME_ATTR, propertyName);
            attrs.put(ServletDAVHandler.ENCODING_ATTR, BASE64_ENCODING);

            xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, SVNXMLUtil.XML_STYLE_PROTECT_CDATA, attrs, null);
            write(xmlBuffer);
            write(value);
            xmlBuffer = SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, null);
            write(xmlBuffer);
        } else {
            xmlBuffer = SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, propertyValue.getString(), NAME_ATTR, propertyName, null);
            write(xmlBuffer);            
        }
    }

    private static class DumpReportHandler extends DAVReportHandler {
        private DAVRequest myDAVRequest;
        
        protected DumpReportHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response) {
            super(connector, request, response);
        }
        
        protected DAVRequest getDAVRequest() {
            if (myDAVRequest == null) {
                myDAVRequest = new DAVRequest() {
                    protected void init() throws SVNException {
                    }
                };
            }
            return myDAVRequest;
        }
    }
}
