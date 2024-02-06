/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import sonia.scm.repository.client.api.RepositoryClientException;

/**
 * Client provider factory for subversion.
 * 
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
      throw new RepositoryClientException("failed to parse svn url", ex);
    }      
    
    // create client
    SVNClientManager client = SVNClientManager.newInstance();
    SVNUpdateClient updateClient = client.getUpdateClient();
    try {
      updateClient.doCheckout(source, workingCopy, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
    } catch (SVNException ex) {
      throw new RepositoryClientException("failed to checkout repository", ex);
    }
    
    // return client provider
    return new SvnRepositoryClientProvider(client, workingCopy);
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
      throw new RepositoryClientException("failed to parse svn url", ex);
    }
    
    // initial checkout
    SVNUpdateClient updateClient = client.getUpdateClient();
    try {
      updateClient.doCheckout(remoteUrl, workingCopy, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
    } catch (SVNException ex) {
      throw new RepositoryClientException("failed to checkout repository", ex);
    }
    
    // return client provider
    return new SvnRepositoryClientProvider(client, workingCopy);
  }

  @Override
  public String getType() {
    return SvnRepositoryHandler.TYPE_NAME;
  }
  
}
