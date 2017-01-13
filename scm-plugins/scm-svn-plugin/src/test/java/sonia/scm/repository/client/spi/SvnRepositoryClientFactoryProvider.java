/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.repository.client.spi;

import java.io.File;
import java.io.IOException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import sonia.scm.repository.SvnRepositoryHandler;

/**
 * Client provider factory for subversion.
 * 
 * @author Sebastian Sdorra
 * @since 1.51
 */
public class SvnRepositoryClientFactoryProvider implements RepositoryClientFactoryProvider {

  @Override
  public RepositoryClientProvider create(File main, File workingCopy) throws IOException {    
    // create uri from file
    SVNURL source;
    try {
      source = SVNURL.fromFile(workingCopy);
    } catch (SVNException ex) {
      throw new IOException("failed to parse svn url", ex);
    }      
    
    // create client
    SVNClientManager client = SVNClientManager.newInstance();
    SVNUpdateClient updateClient = client.getUpdateClient();
    try {
      updateClient.doCheckout(source, workingCopy, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
    } catch (SVNException ex) {
      throw new IOException("failed to checkout repository", ex);
    }
    
    // return client provider
    return new SvnRepositoryClientProvider(client, source, workingCopy);
  }

  @Override
  public RepositoryClientProvider create(String url, String username, String password, File workingCopy) 
    throws IOException {
    
    // create svn options
    DefaultSVNOptions options = new DefaultSVNOptions();
    options.setAuthStorageEnabled(false);
    options.setUseAutoProperties(false);

    // create client
    SVNClientManager client = SVNClientManager.newInstance(new SvnOperationFactory());
    client.setOptions(options);
    if ((username != null) && (password != null)) {
      ISVNAuthenticationManager auth = SVNWCUtil.createDefaultAuthenticationManager(null, username, password, false);
      client.setAuthenticationManager(auth);
    }

    // init dav
    DAVRepositoryFactory.setup();
    
    // parse remote uri
    SVNURL remoteUrl;
    try {
      remoteUrl = SVNURL.parseURIEncoded(url);
    } catch (SVNException ex) {
      throw new IOException("failed to parse svn url", ex);
    }
    
    // initial checkout
    SVNUpdateClient updateClient = client.getUpdateClient();
    try {
      updateClient.doCheckout(remoteUrl, workingCopy, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
    } catch (SVNException ex) {
      throw new IOException("failed to checkout repository", ex);
    }
    
    // return client provider
    return new SvnRepositoryClientProvider(client, remoteUrl, workingCopy);
  }

  @Override
  public String getType() {
    return SvnRepositoryHandler.TYPE_NAME;
  }
  
}
