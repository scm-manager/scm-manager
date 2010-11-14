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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVPathUtil {

    private static final String SLASH = "/";

    public static String dropLeadingSlash(String uri) {
        if (uri == null) {
            return "";
        }
        return uri.startsWith(SLASH) ? uri.substring(SLASH.length()) : uri;
    }

    public static String addLeadingSlash(String uri) {
        if (uri == null) {
            return SLASH;
        }
        return uri.startsWith(SLASH) ? uri : SLASH + uri;
    }

    public static String dropTraillingSlash(String uri) {
        if (uri == null) {
            return "";
        }
        return uri.endsWith(SLASH) ? uri.substring(0, uri.length() - SLASH.length()) : uri;
    }

    public static String addTrailingSlash(String uri) {
        if (uri == null) {
            return SLASH;
        }
        return uri.endsWith(SLASH) ? uri : uri + SLASH;
    }

    public static String head(String uri) {
        uri = dropLeadingSlash(uri);
        int slashIndex = uri.indexOf(SLASH);
        if (slashIndex == -1) {
            return uri;
        }
        return uri.substring(0, slashIndex);
    }

    public static String removeHead(String uri, boolean doStandardize) {
        uri = dropLeadingSlash(uri);
        int headLength = head(uri).length();
        return doStandardize ? standardize(uri.substring(headLength)) : uri.substring(headLength);
    }

    public static String tail(String uri) {
        uri = dropTraillingSlash(uri);
        int lastSlashIndex = uri.lastIndexOf(SLASH);
        if (lastSlashIndex == -1) {
            return uri;
        }
        return uri.substring(lastSlashIndex);
    }

    public static String removeTail(String uri, boolean doStandardize) {
        uri = dropTraillingSlash(uri);
        int tailLength = tail(uri).length();
        return doStandardize ? standardize(uri.substring(0, uri.length() - tailLength)) : uri.substring(0, uri.length() - tailLength);
    }

    public static String append(String parent, String child) {
        StringBuffer uriBuffer = new StringBuffer();
        uriBuffer.append(standardize(parent));
        uriBuffer.append(standardize(child));
        return uriBuffer.toString();
    }

    public static String standardize(String uri) {
        if (uri == null) {
            return SLASH;
        }
        return addLeadingSlash(dropTraillingSlash(uri));
    }

    public static String normalize(String uri) {
        return "".equals(uri) ? SLASH : uri;
    }

    public static void testCanonical(String path) throws DAVException {
        if (path != null && !path.equals(SVNPathUtil.canonicalizePath(path))) {
            throw new DAVException("Path ''{0}'' is not canonicalized;\nthere is a problem with the client.", new Object[] { path }, 
                    HttpServletResponse.SC_BAD_REQUEST, null, SVNLogType.NETWORK, Level.FINE, null, DAVXMLUtil.SVN_DAV_ERROR_TAG, 
                    DAVElement.SVN_DAV_ERROR_NAMESPACE, 0, null);
        }
    }

    public static String buildURI(String context, DAVResourceKind davResourceKind, long revision, String path, boolean addHref) {
        StringBuffer resultURI = new StringBuffer();
        path = path == null ? "" : SVNEncodingUtil.uriEncode(path);
        context = context == null ? "" : context;
        if (addHref) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, 
                    null, resultURI);
        }
        resultURI.append(context);
        resultURI.append(SLASH);
        if (davResourceKind == DAVResourceKind.PUBLIC) {
            resultURI.append(dropLeadingSlash(path));
        } else {
            resultURI.append(DAVResourceURI.SPECIAL_URI).append(SLASH);
            if (davResourceKind == DAVResourceKind.ACT_COLLECTION) {
                resultURI.append(davResourceKind.toString());
                resultURI.append(SLASH);
            } else if (davResourceKind == DAVResourceKind.BASELINE) {
                resultURI.append(davResourceKind.toString());
                resultURI.append(SLASH);
                resultURI.append(String.valueOf(revision));
            } else if (davResourceKind == DAVResourceKind.BASELINE_COLL) {
                resultURI.append(davResourceKind.toString());
                resultURI.append(SLASH);
                resultURI.append(String.valueOf(revision));
                resultURI.append(addLeadingSlash(path));
            } else if (davResourceKind == DAVResourceKind.VERSION) {
                resultURI.append(davResourceKind.toString());
                resultURI.append(SLASH);
                resultURI.append(String.valueOf(revision));
                resultURI.append(addLeadingSlash(path));
            } else if (davResourceKind == DAVResourceKind.VCC) {
                resultURI.append(davResourceKind.toString());
                resultURI.append(SLASH);
                resultURI.append(DAVResourceURI.DEDAULT_VCC_NAME);
            }
        }
        if (addHref) {
            SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), resultURI, false);
        }
        return resultURI.toString();
    }
    
    public static File getActivityPath(File activitiesDB, String activityID) {
        String safeActivityID = SVNFileUtil.computeChecksum(activityID);
        File finalActivityFile = new File(activitiesDB, safeActivityID);
        return finalActivityFile;
    }

    public static DAVURIInfo simpleParseURI(String uri, DAVResource relative) throws SVNException {
        URI parsedURI = null;
        try {
            parsedURI = new URI(uri);
        } catch (URISyntaxException urise) {
            throwMalformedURIErrorException();
        }
        
        String path = parsedURI.getPath();
        if ("".equals(path)) {
            path = "/";
        }
        
        String reposRoot = relative.getResourceURI().getContext();
        
        if (!path.equals(reposRoot) && (!path.startsWith(reposRoot) || path.charAt(reposRoot.length()) != '/')) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.APMOD_MALFORMED_URI, "Unusable URI: it does not refer to this repository");
            SVNErrorManager.error(err, SVNLogType.NETWORK);
        }
        
        long revision = SVNRepository.INVALID_REVISION;
        path = path.substring(reposRoot.length());
        if ("".equals(path) || "/".equals(path)) {
            return new DAVURIInfo(null, "/", revision);
        }
        path = path.substring(1);
        String specialURI = DAVResourceURI.SPECIAL_URI;
        
        if (!path.equals(specialURI) && (!path.startsWith(specialURI) || path.charAt(specialURI.length()) != '/')) {
            path = !path.startsWith("/") ? "/" + path : path; 
            return new DAVURIInfo(null, path, revision);
        }
        
        path = path.substring(specialURI.length());
        if ("".equals(path) || "/".equals(path)) {
            throwUnhandledFormException();
        }

        int slashInd = path.indexOf('/', 1);
        
        if (slashInd == -1 || slashInd == path.length() -1) {
            throwUnhandledFormException();
        }
        
        String segment = path.substring(0, slashInd + 1);
        String activityID = null;
        String reposPath = null;
        if ("/act/".equals(segment)) {
            activityID = path.substring(slashInd + 1);
        } else if ("/ver/".equals(segment)) {
            int nextSlashInd = path.indexOf('/', slashInd + 1);
            if (nextSlashInd == -1) {
                try {
                    revision = Long.parseLong(path.substring(slashInd + 1));
                } catch (NumberFormatException nfe) {
                    throwMalformedURIErrorException();
                }
                reposPath = "/";
            } else {
                segment = path.substring(slashInd + 1, nextSlashInd);
                try {
                    revision = Long.parseLong(segment);
                } catch (NumberFormatException nfe) {
                    throwMalformedURIErrorException();
                }
                reposPath = SVNEncodingUtil.uriDecode(path.substring(nextSlashInd));
            }
            
            if (!SVNRevision.isValidRevisionNumber(revision)) {
                throwMalformedURIErrorException();
            }
        } else {
            throwUnhandledFormException();
        }
        
        return new DAVURIInfo(activityID, reposPath, revision);
    }
    
    private static void throwMalformedURIErrorException() throws SVNException {
        SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.APMOD_MALFORMED_URI, "The specified URI could not be parsed");
        SVNErrorManager.error(err, SVNLogType.NETWORK);
    }
    
    private static void throwUnhandledFormException() throws SVNException {
        SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNSUPPORTED_FEATURE, "Unsupported URI form");
        SVNErrorManager.error(err, SVNLogType.NETWORK);
    }
}
