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
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVIFHeader;
import org.tmatesoft.svn.core.internal.server.dav.DAVIFState;
import org.tmatesoft.svn.core.internal.server.dav.DAVIFStateType;
import org.tmatesoft.svn.core.internal.server.dav.DAVLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockScope;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVValidateWalker implements IDAVResourceWalkHandler {

    public DAVResponse handleResource(DAVResponse response, DAVResource resource, DAVLockInfoProvider lockInfoProvider, LinkedList ifHeaders, 
            int flags, DAVLockScope lockScope, CallType callType) throws DAVException {
        DAVException exception = null;
        try {
            validateResourceState(ifHeaders, resource, lockInfoProvider, lockScope, flags);
            return response;
        } catch (DAVException e) {
            exception = e;
        }
        
        //TODO: I'm not sure what resources we should compare here
        if (DAVServlet.isHTTPServerError(exception.getResponseCode())) {
            throw exception;
        }
        
        DAVResponse resp = new DAVResponse(null, resource.getResourceURI().getRequestURI(), response, null, exception.getResponseCode());        
        return resp;
    }

    public void validateResourceState(LinkedList ifHeaders, DAVResource resource, DAVLockInfoProvider provider, DAVLockScope lockScope, int flags) throws DAVException {
        DAVLock lock = null;
        if (provider != null) {
            try {
                lock = provider.getLock(resource);
            } catch (DAVException dave) {
                throw new DAVException("The locks could not be queried for verification against a possible \"If:\" header.", null, 
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, SVNLogType.NETWORK, Level.FINE, dave, null, null, 0, null);
            }
        }
    
        boolean seenLockToken = false;
        if (lockScope == DAVLockScope.EXCLUSIVE) {
            if (lock != null) {
                throw new DAVException("Existing lock(s) on the requested resource prevent an exclusive lock.", ServletDAVHandler.SC_HTTP_LOCKED, 0);
            }
            seenLockToken = true;
        } else if (lockScope == DAVLockScope.SHARED) {
            if (lock.getScope() == DAVLockScope.EXCLUSIVE) {
                throw new DAVException("The requested resource is already locked exclusively.", ServletDAVHandler.SC_HTTP_LOCKED, 0);
            }
            seenLockToken = true;
        } else {
            seenLockToken = lock == null;
        }
        
        if (ifHeaders == null || ifHeaders.isEmpty()) {
            if (seenLockToken) {
                return;
            }
            
            throw new DAVException("This resource is locked and an \"If:\" header was not supplied to allow access to the resource.", 
                    ServletDAVHandler.SC_HTTP_LOCKED, 0);
        }
        
        DAVIFHeader ifHeader = (DAVIFHeader) ifHeaders.getFirst();
        if (lock == null && ifHeader.isDummyHeader()) {
            if ((flags & ServletDAVHandler.DAV_VALIDATE_IS_PARENT) != 0) {
                return;
            }
            throw new DAVException("The locktoken specified in the \"Lock-Token:\" header is invalid because this resource has no outstanding locks.", 
                    HttpServletResponse.SC_BAD_REQUEST, 0);
        }

        String eTag = resource.getETag();
        String uri = DAVPathUtil.dropTraillingSlash(resource.getResourceURI().getRequestURI());
        
        int numThatAppy = 0;
        String reason = null;
        Iterator ifHeadersIter = ifHeaders.iterator();
        for (;ifHeadersIter.hasNext();) {
            ifHeader = (DAVIFHeader) ifHeadersIter.next();
            if (ifHeader.getURI() != null && !uri.equals(ifHeader.getURI())) {
                continue;
            }
            
            ++numThatAppy;
            LinkedList stateList = ifHeader.getStateList();
            boolean doContinue = false;
            for (Iterator stateListIter = stateList.iterator(); stateListIter.hasNext();) {
                DAVIFState state = (DAVIFState) stateListIter.next();
                if (state.getType() == DAVIFStateType.IF_ETAG) {
                    String currentETag = null;
                    String givenETag = null;
                    String stateETag = state.getETag();
                    if (stateETag.startsWith("W/")) {
                        givenETag = stateETag.substring(2);
                    } else {
                        givenETag = stateETag;
                    }
                    
                    if (eTag.startsWith("W/")) {
                        currentETag = eTag.substring(2);
                    } else {
                        currentETag = eTag;
                    }
                    
                    boolean eTagsDoNotMatch = !givenETag.equals(currentETag);
                    
                    if (state.getCondition() == DAVIFState.IF_CONDITION_NORMAL && eTagsDoNotMatch) {
                        reason = "an entity-tag was specified, but the resource's actual ETag does not match."; 
                        doContinue = true;
                        break; 
                    } else if (state.getCondition() == DAVIFState.IF_CONDITION_NOT && !eTagsDoNotMatch) {
                        reason = "an entity-tag was specified using the \"Not\" form, but the resource's actual ETag matches the provided entity-tag.";
                        doContinue = true;
                        break;
                    }
                } else if (state.getType() == DAVIFStateType.IF_OPAQUE_LOCK) {
                    if (provider == null) {
                        if (state.getCondition() == DAVIFState.IF_CONDITION_NOT) {
                            continue;
                        }
                        
                        reason = "a State-token was supplied, but a lock database is not available for to provide the required lock.";
                        doContinue = true;
                        break;
                    }
                    
                    boolean matched = false;
                    if (lock != null) {
                        if (!lock.getLockToken().equals(state.getLockToken())) {
                            continue;
                        }
                        
                        seenLockToken = true;
                        
                        if (state.getCondition() == DAVIFState.IF_CONDITION_NOT) {
                            reason = "a State-token was supplied, which used a \"Not\" condition. The State-token was found in the locks on this resource";
                            doContinue = true;
                            break;
                        }
                        
                        String lockAuthUser = lock.getAuthUser(); 
                        String requestUser = resource.getUserName();
                        if (lockAuthUser != null && (requestUser == null || !lockAuthUser.equals(requestUser))) {
                            throw new DAVException("User \"{0}\" submitted a locktoken created by user \"{1}\".", 
                                    new Object[] { requestUser, lockAuthUser }, HttpServletResponse.SC_FORBIDDEN, 0);
                        }
                        
                        matched = true;
                    }
                    
                    if (!matched && state.getCondition() == DAVIFState.IF_CONDITION_NORMAL) {
                        reason = "a State-token was supplied, but it was not found in the locks on this resource.";
                        doContinue = true;
                        break;
                    }
                } else if (state.getType() == DAVIFStateType.IF_UNKNOWN) {
                    if (state.getCondition() == DAVIFState.IF_CONDITION_NORMAL) {
                        reason = "an unknown state token was supplied";
                        doContinue = true;
                        break;
                    }
                }
            }
            
            if (doContinue) {
                continue;
            }
            
            if (seenLockToken) {
                return;
            }
            
            break;
        }
        
        if (!ifHeadersIter.hasNext()) {
            if (numThatAppy == 0) {
                if (seenLockToken) {
                    return;
                }
                
                if (findSubmittedLockToken(ifHeaders, lock)) {
                    return;
                }
                
                throw new DAVException("This resource is locked and the \"If:\" header did not specify one of the locktokens for this resource's lock(s).", 
                        ServletDAVHandler.SC_HTTP_LOCKED, 0);
            }
            
            ifHeader = (DAVIFHeader) ifHeaders.getFirst();
            if (ifHeader.isDummyHeader()) {
                throw new DAVException("The locktoken specified in the \"Lock-Token:\" header did not specify one of this resource's locktoken(s).", 
                        HttpServletResponse.SC_BAD_REQUEST, 0);
            }
            
            if (reason == null) {
                throw new DAVException("The preconditions specified by the \"If:\" header did not match this resource.", 
                        HttpServletResponse.SC_PRECONDITION_FAILED, 0);
            }

            throw new DAVException("The precondition(s) specified by the \"If:\" header did not match this resource. At least one failure is because: {0}", 
                    new Object[] { reason }, HttpServletResponse.SC_PRECONDITION_FAILED, 0);
        }
        
        if (findSubmittedLockToken(ifHeaders, lock)) {
            return;
        }
        
        if (ifHeader.isDummyHeader()) {
            throw new DAVException("The locktoken specified in the \"Lock-Token:\" header did not specify one of this resource's locktoken(s).", 
                    HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        throw new DAVException("This resource is locked and the \"If:\" header did not specify one of the locktokens for this resource's lock(s).", 
                ServletDAVHandler.SC_HTTP_LOCKED, 1);
    }
    
    private boolean findSubmittedLockToken(LinkedList ifHeaders, DAVLock lock) {
        for (Iterator ifHeadersIter = ifHeaders.iterator(); ifHeadersIter.hasNext();) {
            DAVIFHeader ifHeader = (DAVIFHeader) ifHeadersIter.next();
            LinkedList ifStates = ifHeader.getStateList(); 
            for (Iterator ifStatesIter = ifStates.iterator(); ifStatesIter.hasNext();) {
                DAVIFState ifState = (DAVIFState) ifStatesIter.next();
                if (ifState.getType() == DAVIFStateType.IF_OPAQUE_LOCK) {
                    String lockToken = lock.getLockToken();
                    String stateLockToken = ifState.getLockToken();
                    if (lockToken.equals(stateLockToken)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
