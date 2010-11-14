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
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVHandlerFactory;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVRepositoryManager {
    
    private static final String FILE_PROTOCOL_LINE = "file://";
    private static final String DESTINATION_HEADER = "Destination";
    private static final String DEFAULT_ACTIVITY_DB = "dav/activities.d";

    private DAVConfig myDAVConfig;

    private String myResourceRepositoryRoot;
    private String myResourceContext;
    private String myResourcePathInfo;
    private Principal myUserPrincipal;
    private File myRepositoryRootDir;
    
    public DAVRepositoryManager(DAVConfig config, HttpServletRequest request) throws SVNException {
        if (config == null) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_INVALID_CONFIG_VALUE), SVNLogType.NETWORK);
        }

        myDAVConfig = config;

        myResourceRepositoryRoot = getRepositoryRoot(request.getPathInfo());
        myResourceContext = getResourceContext(request);
        myUserPrincipal = request.getUserPrincipal();
        myRepositoryRootDir = getRepositoryRootDir(request.getPathInfo());
        myResourcePathInfo = getResourcePathInfo(request);
            
        if (config.isUsingPBA()) {
            String path = null;
            if (!DAVHandlerFactory.METHOD_MERGE.equals(request.getMethod())) {
                DAVResourceURI tmp = new DAVResourceURI(null, myResourcePathInfo, null, false);
                path = DAVPathUtil.standardize(tmp.getPath());
            }

            boolean checkDestinationPath = false;
            String destinationPath = null;
            if (DAVHandlerFactory.METHOD_MOVE.equals(request.getMethod()) || DAVHandlerFactory.METHOD_COPY.equals(request.getMethod())) {
                String destinationURL = request.getHeader(DESTINATION_HEADER);
                if (destinationURL == null) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Destination path missing"), SVNLogType.NETWORK);
                }
                destinationPath = DAVPathUtil.standardize(getRepositoryRelativePath(SVNURL.parseURIEncoded(destinationURL)));
                checkDestinationPath = true;
            }

            String repository = getResourceRepositoryName(request.getPathInfo());
            String user = request.getRemoteUser();
            int access = getRequestedAccess(request.getMethod());
            checkAccess(repository, path, checkDestinationPath, destinationPath, user, access);
        }
    }

    private int getRequestedAccess(String method) {
        int access = SVNPathBasedAccess.SVN_ACCESS_NONE;
        if (DAVHandlerFactory.METHOD_COPY.equals(method) ||
                DAVHandlerFactory.METHOD_MOVE.equals(method) ||
                DAVHandlerFactory.METHOD_DELETE.equals(method)) {
            access |= SVNPathBasedAccess.SVN_ACCESS_RECURSIVE;
        } else if (DAVHandlerFactory.METHOD_OPTIONS.equals(method) ||
                DAVHandlerFactory.METHOD_PROPFIND.equals(method) ||
                DAVHandlerFactory.METHOD_GET.equals(method) ||
                DAVHandlerFactory.METHOD_REPORT.equals(method)) {
            access |= SVNPathBasedAccess.SVN_ACCESS_READ;
        } else if (DAVHandlerFactory.METHOD_MKCOL.equals(method) ||
                DAVHandlerFactory.METHOD_PUT.equals(method) ||
                DAVHandlerFactory.METHOD_PROPPATCH.equals(method) ||
                DAVHandlerFactory.METHOD_CHECKOUT.equals(method) ||
                DAVHandlerFactory.METHOD_MERGE.equals(method) ||
                DAVHandlerFactory.METHOD_MKACTIVITY.equals(method) ||
                DAVHandlerFactory.METHOD_LOCK.equals(method) ||
                DAVHandlerFactory.METHOD_UNLOCK.equals(method)) {
            access |= SVNPathBasedAccess.SVN_ACCESS_WRITE;
        } else {
            access |= SVNPathBasedAccess.SVN_ACCESS_RECURSIVE | SVNPathBasedAccess.SVN_ACCESS_WRITE;
        }
        return access;
    }

    private void checkAccess(String repository, String path, boolean checkDestinationPath, String destinationPath, String user, int access) throws SVNException {
        if (getDAVConfig().getSVNAccess() == null) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_INVALID_CONFIG_VALUE, "An error occured while loading configuration file."), SVNLogType.NETWORK);
        }
        if (!getDAVConfig().isAnonymousAllowed() && user == null) {
            SVNErrorManager.authenticationFailed("Anonymous user is not allowed on resource", null);
        }

        if (path != null || (path == null && (access & SVNPathBasedAccess.SVN_ACCESS_WRITE) != SVNPathBasedAccess.SVN_ACCESS_NONE)) {
            if (!getDAVConfig().getSVNAccess().checkAccess(repository, path, user, access)) {
                if (user == null) {
                    SVNErrorManager.authenticationFailed("Forbidden for anonymous", null);
                } else {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.NO_AUTH_FILE_PATH), SVNLogType.NETWORK);
                }
            }
        }

        if (checkDestinationPath) {
            if (path != null) {
                if (!getDAVConfig().getSVNAccess().checkAccess(repository, destinationPath, user, access)) {
                    if (user == null) {
                        SVNErrorManager.authenticationFailed("Forbidden for anonymous", null);
                    } else {
                        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.NO_AUTH_FILE_PATH), SVNLogType.NETWORK);
                    }
                }
            }
        }

    }

    public DAVConfig getDAVConfig() {
        return myDAVConfig;
    }

    public String getResourceRepositoryRoot() {
        return myResourceRepositoryRoot;
    }
    
    public String getResourceContext() {
        return myResourceContext;
    }

    public String getResourcePathInfo() {
        return myResourcePathInfo;
    }

    public SVNURL convertHttpToFile(SVNURL url) throws SVNException {
        String uri = DAVPathUtil.addLeadingSlash(url.getURIEncodedPath());
        if (!uri.startsWith(getResourceContext())) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Invalid URL ''{0}'' requested", url.toString()), SVNLogType.NETWORK);
        }
        return SVNURL.parseURIEncoded(getResourceRepositoryRoot() + getRepositoryRelativePath(url));
    }

    public String getRepositoryRelativePath(SVNURL url) throws SVNException {
        String uri = getURI(url);
        DAVResourceURI resourceURI = new DAVResourceURI(null, uri, null, false);
        return resourceURI.getPath();
    }

    public String getURI(SVNURL url) throws SVNException {
        String uri = DAVPathUtil.addLeadingSlash(url.getURIEncodedPath());
        if (uri.startsWith(getResourceContext())) {
            uri = uri.substring(getResourceContext().length());
        } else {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Invalid URL ''{0}'' requested", url.toString()), 
                    SVNLogType.NETWORK);
        }
        return uri;
    }

    public DAVResource getRequestedDAVResource(boolean isSVNClient, String deltaBase, String pathInfo, long version, String clientOptions,
            String baseChecksum, String resultChecksum, String label, boolean useCheckedIn, List lockTokens, Map capabilities) throws SVNException {
        pathInfo = pathInfo == null ? getResourcePathInfo() : pathInfo;
        DAVResourceURI resourceURI = new DAVResourceURI(getResourceContext(), pathInfo, label, useCheckedIn);
        DAVConfig config = getDAVConfig();
        String fsParentPath = config.getRepositoryParentPath();
        String xsltURI = config.getXSLTIndex();
        String reposName = config.getRepositoryName();
        String uri = resourceURI.getURI();

        if (fsParentPath != null && getDAVConfig().isListParentPath()) {
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            //TODO: later add code for parent path resource here
        }
        
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "uri type - " + resourceURI.getType() + ", uri kind - " + resourceURI.getKind());
        
        String activitiesDB = config.getActivitiesDBPath();
        File activitiesDBDir = null;
        if (activitiesDB == null) {
            activitiesDBDir = new File(myRepositoryRootDir, DEFAULT_ACTIVITY_DB); 
        } else {
            activitiesDBDir = new File(activitiesDB);
        }
        
        String userName = myUserPrincipal != null ? myUserPrincipal.getName() : null;
        SVNAuthentication auth = new SVNUserNameAuthentication(userName, false, null, false);
        BasicAuthenticationManager authManager = new BasicAuthenticationManager(new SVNAuthentication[] { auth });
        SVNRepository resourceRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(getResourceRepositoryRoot()));
        resourceRepository.setAuthenticationManager(authManager);
        DAVResource resource = new DAVResource(resourceRepository, this, resourceURI, isSVNClient, deltaBase, version, 
                clientOptions, baseChecksum, resultChecksum, userName, activitiesDBDir, lockTokens, capabilities);
        return resource;
    }

    private String getRepositoryRoot(String requestURI) {
        StringBuffer repositoryURL = new StringBuffer();
        repositoryURL.append(FILE_PROTOCOL_LINE);

        if (getDAVConfig().isUsingRepositoryPathDirective()) {
            repositoryURL.append(getDAVConfig().getRepositoryPath().startsWith("/") ? "" : "/");
            repositoryURL.append(getDAVConfig().getRepositoryPath());
        } else {
            String reposParentPath = getDAVConfig().getRepositoryParentPath();
            if (!reposParentPath.startsWith("/")) {
                reposParentPath = "/" + reposParentPath;
            }
            
            repositoryURL.append(DAVPathUtil.addTrailingSlash(reposParentPath));
            repositoryURL.append(DAVPathUtil.head(requestURI));
        }
        return repositoryURL.toString();
    }

    private File getRepositoryRootDir(String requestURI) {
        File reposRootDir = null;
        if (getDAVConfig().isUsingRepositoryPathDirective()) {
            reposRootDir = new File(getDAVConfig().getRepositoryPath());
        } else {
            reposRootDir = new File(getDAVConfig().getRepositoryParentPath(), DAVPathUtil.head(requestURI));
        }
        return reposRootDir;
    }

    private String getResourcePathInfo(HttpServletRequest request) throws SVNException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "".equals(pathInfo)) {
            pathInfo = "/";
        }
        
        if (getDAVConfig().isUsingRepositoryPathDirective()) {
            return pathInfo;
        }   

        if (pathInfo == null || pathInfo.length() == 0 || "/".equals(pathInfo)) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED), SVNLogType.NETWORK);
            //TODO: client tried to access repository parent path, result status code should be FORBIDDEN.
        }
        return DAVPathUtil.removeHead(pathInfo, true);
    }

    private String getResourceRepositoryName(String requestURI) {
        if (getDAVConfig().isUsingRepositoryPathDirective()) {
            return "";
        } 
        return DAVPathUtil.head(requestURI);        
    }

    private String getResourceContext(HttpServletRequest request) {
        String requestContext = request.getContextPath();
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        
        if (getDAVConfig().isUsingRepositoryPathDirective()) {
            if (servletPath != null && !"".equals(servletPath)) {
                if (servletPath.startsWith("/")) {
                    servletPath = servletPath.substring(1);
                }
                requestContext = SVNPathUtil.append(requestContext, servletPath);
            }
            return SVNEncodingUtil.uriEncode(requestContext);
        }
        
        String reposName = DAVPathUtil.head(pathInfo);
        if (servletPath != null && !"".equals(servletPath)) {
            if (servletPath.startsWith("/")) {
                servletPath = servletPath.substring(1);
            }
            String pathToRepos = SVNPathUtil.append(requestContext, servletPath);
            requestContext = SVNPathUtil.append(pathToRepos, reposName);
            return SVNEncodingUtil.uriEncode(requestContext);
        }
        requestContext = DAVPathUtil.append(requestContext, reposName);
        return SVNEncodingUtil.uriEncode(requestContext);
    }
}
