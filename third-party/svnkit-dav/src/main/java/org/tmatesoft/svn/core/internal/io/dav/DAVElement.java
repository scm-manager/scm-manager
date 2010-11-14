/*
 * ====================================================================
 * Copyright (c) 2004-2009 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

package org.tmatesoft.svn.core.internal.io.dav;

import java.util.Map;

import org.tmatesoft.svn.core.internal.io.dav.http.XMLReader;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVElement {

    private static Map ourProperties = new SVNHashMap();

    public static DAVElement getElement(String namespace, String name) {
        if (namespace == null) {
            namespace = "";
        }
        Map properties = (Map) ourProperties.get(namespace);
        if (properties == null) {
            properties = new SVNHashMap();
            ourProperties.put(namespace, properties);
        }
        name = name.replace(XMLReader.COLON_REPLACEMENT, ':');
        DAVElement property = (DAVElement) properties.get(name);
        if (property == null) {
            property = new DAVElement(namespace, name);
            properties.put(name, property);
        }
        return property;
    }

    public static final String SVN_DAV_PROPERTY_NAMESPACE = "http://subversion.tigris.org/xmlns/dav/";
    public static final String SVN_CUSTOM_PROPERTY_NAMESPACE = "http://subversion.tigris.org/xmlns/custom/";
    public static final String SVN_SVN_PROPERTY_NAMESPACE = "http://subversion.tigris.org/xmlns/svn/";
    public static final String SVN_APACHE_PROPERTY_NAMESPACE = "http://apache.org/dav/xmlns";

    public static final String SVN_DAV_ERROR_NAMESPACE = "svn:";
    public static final String DAV_NAMESPACE = "DAV:";
    public static final String SVN_NAMESPACE = "svn:";

    public static final String DEPTH_OPTION = SVN_DAV_PROPERTY_NAMESPACE + "svn/depth";
    public static final String MERGE_INFO_OPTION = SVN_DAV_PROPERTY_NAMESPACE + "svn/mergeinfo";
    public static final String LOG_REVPROPS_OPTION = SVN_DAV_PROPERTY_NAMESPACE + "svn/log-revprops";
    public static final String PARTIAL_REPLAY_OPTION = SVN_DAV_PROPERTY_NAMESPACE + "svn/partial-replay";

    public static final DAVElement ACTIVITY = getElement(DAV_NAMESPACE, "activity");
    public static final DAVElement VERSION_HISTORY = getElement(DAV_NAMESPACE, "version-history");
    public static final DAVElement DISPLAY_NAME = getElement(DAV_NAMESPACE, "displayname");
    public static final DAVElement SUPPORTED_LIVE_PROPERTY = getElement(DAV_NAMESPACE, "supported-live-property");
    public static final DAVElement MERGE_RESPONSE = getElement(DAV_NAMESPACE, "merge-response");
    public static final DAVElement UPDATE_SET = getElement(DAV_NAMESPACE, "updated-set");
    public static final DAVElement NO_AUTO_MERGE = getElement(DAV_NAMESPACE, "no-auto-merge");
    public static final DAVElement NO_CHECKOUT = getElement(DAV_NAMESPACE, "no-checkout");
    public static final DAVElement SOURCE = getElement(DAV_NAMESPACE, "source");
    public static final DAVElement MULTISTATUS = getElement(DAV_NAMESPACE, "multistatus");
    public static final DAVElement RESPONSE = getElement(DAV_NAMESPACE, "response");
    public static final DAVElement RESPONSE_DESCRIPTION = getElement(DAV_NAMESPACE, "responsedescription");
    public static final DAVElement HREF = getElement(DAV_NAMESPACE, "href");
    public static final DAVElement PROPSTAT = getElement(DAV_NAMESPACE, "propstat");
    public static final DAVElement PROP = getElement(DAV_NAMESPACE, "prop");
    public static final DAVElement STATUS = getElement(DAV_NAMESPACE, "status");
    public static final DAVElement BASELINE = getElement(DAV_NAMESPACE, "baseline");
    public static final DAVElement BASELINE_COLLECTION = getElement(DAV_NAMESPACE, "baseline-collection");
    public static final DAVElement CHECKED_IN = getElement(DAV_NAMESPACE, "checked-in");
    public static final DAVElement COLLECTION = getElement(DAV_NAMESPACE, "collection");
    public static final DAVElement RESOURCE_TYPE = getElement(DAV_NAMESPACE, "resourcetype");
    public static final DAVElement VERSION_CONTROLLED_CONFIGURATION = getElement(DAV_NAMESPACE, "version-controlled-configuration");
    public static final DAVElement VERSION_NAME = getElement(DAV_NAMESPACE, "version-name");
    public static final DAVElement GET_CONTENT_LENGTH = getElement(DAV_NAMESPACE, "getcontentlength");
    public static final DAVElement CREATION_DATE = getElement(DAV_NAMESPACE, "creationdate");
    public static final DAVElement CREATOR_DISPLAY_NAME = getElement(DAV_NAMESPACE, "creator-displayname");
    public static final DAVElement COMMENT = getElement(DAV_NAMESPACE, "comment");
    public static final DAVElement DATE = getElement(SVN_NAMESPACE, "date");
    public static final DAVElement POST_COMMIT_ERROR = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "post-commit-err");
    public static final DAVElement PROPFIND = DAVElement.getElement(DAV_NAMESPACE, "propfind");
    public static final DAVElement ALLPROP = DAVElement.getElement(DAV_NAMESPACE, "allprop");
    public static final DAVElement PROPNAME = DAVElement.getElement(DAV_NAMESPACE, "propname");
    public static final DAVElement ACTIVE_LOCK = DAVElement.getElement(DAV_NAMESPACE, "activelock");
    public static final DAVElement LOCK_TYPE = DAVElement.getElement(DAV_NAMESPACE, "locktype");
    public static final DAVElement LOCK_SCOPE = DAVElement.getElement(DAV_NAMESPACE, "lockscope");
    public static final DAVElement WRITE = DAVElement.getElement(DAV_NAMESPACE, "write");
    public static final DAVElement EXCLUSIVE = DAVElement.getElement(DAV_NAMESPACE, "exclusive");
    public static final DAVElement SHARED = DAVElement.getElement(DAV_NAMESPACE, "shared");
    public static final DAVElement DEPTH = DAVElement.getElement(DAV_NAMESPACE, "depth");

    public static final DAVElement SUPPORTED_LOCK = getElement(DAV_NAMESPACE, "supportedlock");
    public static final DAVElement LOCK_DISCOVERY = getElement(DAV_NAMESPACE, "lockdiscovery");
    public static final DAVElement LOCK_OWNER = getElement(DAV_NAMESPACE, "owner");
    public static final DAVElement LOCK_TIMEOUT = getElement(DAV_NAMESPACE, "timeout");
    public static final DAVElement LOCK_TOKEN = getElement(DAV_NAMESPACE, "locktoken");
    public static final DAVElement LOCK_ENTRY = getElement(DAV_NAMESPACE, "lockentry");

    public static final DAVElement SVN_LOCK_TOKEN_LIST = getElement(SVN_NAMESPACE, "lock-token-list");
    public static final DAVElement SVN_LOCK = getElement(SVN_NAMESPACE, "lock");
    public static final DAVElement SVN_LOCK_PATH = getElement(SVN_NAMESPACE, "path");
    public static final DAVElement SVN_LOCK_TOKEN = getElement(SVN_NAMESPACE, "token");
    public static final DAVElement SVN_LOCK_COMMENT = getElement(SVN_NAMESPACE, "comment");
    public static final DAVElement SVN_LOCK_OWNER = getElement(SVN_NAMESPACE, "owner");
    public static final DAVElement SVN_LOCK_CREATION_DATE = getElement(SVN_NAMESPACE, "creationdate");
    public static final DAVElement SVN_LOCK_EXPIRATION_DATE = getElement(SVN_NAMESPACE, "expirationdate");

    //servlet defined svn namespace properties
    public static final DAVElement PATH = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "path");
    public static final DAVElement REVISION = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "revision");
    public static final DAVElement START_REVISION = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "start-revision");
    public static final DAVElement END_REVISION = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "end-revision");
    public static final DAVElement PEG_REVISION = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "peg-revision");
    public static final DAVElement INCLUDE_MERGED_REVISIONS = getElement(SVN_NAMESPACE, "include-merged-revisions");

    public static final DAVElement BASELINE_RELATIVE_PATH = getElement(SVN_DAV_PROPERTY_NAMESPACE, "baseline-relative-path");
    public static final DAVElement REPOSITORY_UUID = getElement(SVN_DAV_PROPERTY_NAMESPACE, "repository-uuid");
    public static final DAVElement MD5_CHECKSUM = getElement(SVN_DAV_PROPERTY_NAMESPACE, "md5-checksum");
    public static final DAVElement DEADPROP_COUNT = getElement(SVN_DAV_PROPERTY_NAMESPACE, "deadprop-count");

    public static final DAVElement AUTO_VERSION = getElement(DAV_NAMESPACE, "auto-version");

    public static final DAVElement MERGE_INFO_ITEM = getElement(SVN_NAMESPACE, "mergeinfo-item");
    public static final DAVElement MERGE_INFO_PATH = getElement(SVN_NAMESPACE, "mergeinfo-path");
    public static final DAVElement MERGE_INFO_INFO = getElement(SVN_NAMESPACE, "mergeinfo-info");

    //Supported live properties
    public static final DAVElement GET_CONTENT_LANGUAGE = getElement(DAV_NAMESPACE, "getcontentlanguage");
    public static final DAVElement GET_CONTENT_TYPE = getElement(DAV_NAMESPACE, "getcontenttype");
    public static final DAVElement GET_ETAG = getElement(DAV_NAMESPACE, "getetag");
    public static final DAVElement GET_LAST_MODIFIED = getElement(DAV_NAMESPACE, "getlastmodified");

    public static final DAVElement[] STARTING_PROPERTIES = {VERSION_CONTROLLED_CONFIGURATION, RESOURCE_TYPE, BASELINE_RELATIVE_PATH, REPOSITORY_UUID};
    public static final DAVElement[] BASELINE_PROPERTIES = {BASELINE_COLLECTION, VERSION_NAME};

    private String myPropertyName;
    private String myNamespace;
    public static final DAVElement LOG = getElement(SVN_SVN_PROPERTY_NAMESPACE, "log");

    private DAVElement(String namespace, String propertyName) {
        myNamespace = namespace;
        myPropertyName = propertyName;
    }

    public String getNamespace() {
        return myNamespace;
    }

    public String getName() {
        return myPropertyName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getNamespace());
        if (!getNamespace().endsWith(":")) {
            sb.append(":");
        }
        sb.append(getName());
        return sb.toString();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((myNamespace == null) ? 0 : myNamespace.hashCode());
        result = prime * result + ((myPropertyName == null) ? 0 : myPropertyName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        DAVElement other = (DAVElement) obj;
        if (myNamespace == null) {
            if (other.myNamespace != null) {
                return false;
            }
        } else if (!myNamespace.equals(other.myNamespace)) {
            return false;
        }

        if (myPropertyName == null) {
            if (other.myPropertyName != null) {
                return false;
            }
        } else if (!myPropertyName.equals(other.myPropertyName)) {
            return false;
        }

        return true;
    }

}
