/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
