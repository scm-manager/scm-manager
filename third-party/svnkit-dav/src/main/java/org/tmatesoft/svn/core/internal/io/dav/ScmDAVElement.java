/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */
package org.tmatesoft.svn.core.internal.io.dav;

import static org.tmatesoft.svn.core.internal.io.dav.DAVElement.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmDAVElement
{
  
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
  
}
