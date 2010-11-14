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

import javax.servlet.ServletConfig;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVConfig {

    private static final String PATH_DIRECIVE = "SVNPath";
    private static final String PARENT_PATH_DIRECIVE = "SVNParentPath";
    private static final String SVN_ACCESS_FILE_DIRECTIVE = "AuthzSVNAccessFile";
    private static final String SVN_ANONYMOUS_DIRECTIVE = "AuthzSVNAnonymous";
    private static final String SVN_NO_AUTH_IF_ANONYMOUS_ALLOWED_DIRECIVE = "AuthzSVNNoAuthWhenAnonymousAllowed";
    private static final String LIST_PARENT_PATH_DIRECTIVE = "SVNListParentPath";
    private static final String REPOS_NAME = "SVNReposName";
    private static final String XSLT_INDEX = "SVNIndexXSLT";
    private static final String ACTIVITIES_DB = "SVNActivitiesDB"; 
    private static final String AUTOVERSIONING = "SVNAutoversioning";
    private static final String ALLOW_BULK_UPDATES = "SVNAllowBulkUpdates";
    private static final String DAV_DEPTH = "DAVDepthInfinity";
    private static final String OFF = "off";
    private static final String ON = "on";

    private String myRepositoryPath;
    private String myRepositoryParentPath;
    private String myRepositoryName;
    private String myXSLTIndex;
    private String myActivitiesDBPath;
    private SVNPathBasedAccess mySVNAccess = null;
    private boolean myUsingPBA = false;
    private boolean myAnonymous = true;
    private boolean myNoAuthIfAnonymousAllowed = false;
    private boolean myIsListParentPath = false;
    private boolean myIsAutoVersioning = false;
    private boolean myIsAllowBulkUpdates = false;
    private boolean myIsAllowDepthInfinity = false;

    // scm-manager change
    public DAVConfig()
    {
    }
    
    public DAVConfig(ServletConfig servletConfig) throws SVNException {
        String repositoryPath = servletConfig.getInitParameter(PATH_DIRECIVE);
        String repositoryParentPath = servletConfig.getInitParameter(PARENT_PATH_DIRECIVE);
        myRepositoryName = servletConfig.getInitParameter(REPOS_NAME);
        myXSLTIndex = servletConfig.getInitParameter(XSLT_INDEX);
        
        if (repositoryPath != null && repositoryParentPath == null) {
            myRepositoryPath = repositoryPath;
            myRepositoryParentPath = null;
        } else if (repositoryParentPath != null && repositoryPath == null) {
            myRepositoryParentPath = repositoryParentPath;
            myRepositoryPath = null;
        } else {
            //repositoryPath == null <=> repositoryParentPath == null.
            if (repositoryPath == null) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_INVALID_CONFIG_VALUE, 
                        "Neither SVNPath nor SVNParentPath directive were specified."), SVNLogType.NETWORK);
            } else {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_INVALID_CONFIG_VALUE, 
                        "Only one of SVNPath and SVNParentPath directives should be specified."), SVNLogType.NETWORK);
            }
        }

        String configurationFilePath = servletConfig.getInitParameter(SVN_ACCESS_FILE_DIRECTIVE);
        if (configurationFilePath != null) {
            myUsingPBA = true;
            try {
                mySVNAccess = new SVNPathBasedAccess(new File(configurationFilePath));
            } catch (SVNException e) {
                mySVNAccess = null;
            }
        }

        String anonymous = servletConfig.getInitParameter(SVN_ANONYMOUS_DIRECTIVE);
        if (anonymous != null && OFF.equals(anonymous)) {
            myAnonymous = false;
        }

        String noAuthIfAnonymousAllowed = servletConfig.getInitParameter(SVN_NO_AUTH_IF_ANONYMOUS_ALLOWED_DIRECIVE);
        if (noAuthIfAnonymousAllowed != null && ON.equals(noAuthIfAnonymousAllowed)) {
            myNoAuthIfAnonymousAllowed = true;
        }
        
        String listParentPath = servletConfig.getInitParameter(LIST_PARENT_PATH_DIRECTIVE);
        if (listParentPath != null && ON.equals(listParentPath)) {
            myIsListParentPath = true;
        }
        
        String autoversioning = servletConfig.getInitParameter(AUTOVERSIONING);
        if (autoversioning != null && ON.equals(autoversioning)) {
            myIsAutoVersioning = true;
        }
        
        String allowBulkUpdates = servletConfig.getInitParameter(ALLOW_BULK_UPDATES);
        if (allowBulkUpdates != null && ON.equals(allowBulkUpdates)) {
            myIsAllowBulkUpdates = true;
        }
    
        String allowDepthInfinity = servletConfig.getInitParameter(DAV_DEPTH);
        if (allowDepthInfinity != null && ON.equals(allowDepthInfinity)) {
            myIsAllowDepthInfinity = true;
        }
        
        myActivitiesDBPath = servletConfig.getInitParameter(ACTIVITIES_DB);
    }
    
    public boolean isAllowDepthInfinity() {
        return myIsAllowDepthInfinity;
    }

    public String getRepositoryName() {
        return myRepositoryName;
    }
    
    public String getXSLTIndex() {
        return myXSLTIndex;
    }

    public boolean isUsingRepositoryPathDirective() {
        return myRepositoryPath != null;
    }

    public String getRepositoryPath() {
        return myRepositoryPath;
    }

    public String getRepositoryParentPath() {
        return myRepositoryParentPath;
    }

    public SVNPathBasedAccess getSVNAccess() {
        return mySVNAccess;
    }

    public boolean isUsingPBA() {
        return myUsingPBA;
    }

    public boolean isAnonymousAllowed() {
        return myAnonymous;
    }

    public boolean isNoAuthIfAnonymousAllowed() {
        return myNoAuthIfAnonymousAllowed;
    }
    
    public boolean isListParentPath() {
        return myIsListParentPath;
    }

    public String getActivitiesDBPath() {
        return myActivitiesDBPath;
    }

    public boolean isAutoVersioning() {
        return myIsAutoVersioning;
    }

    public boolean isAllowBulkUpdates() {
        return myIsAllowBulkUpdates;
    }

}
