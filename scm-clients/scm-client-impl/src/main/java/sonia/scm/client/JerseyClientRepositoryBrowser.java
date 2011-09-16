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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientRepositoryBrowser implements ClientRepositoryBrowser
{

  /**
   * Constructs ...
   *
   *
   * @param session
   * @param repository
   */
  public JerseyClientRepositoryBrowser(JerseyClientSession session,
          Repository repository)
  {
    this.session = session;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public InputStream getContent(String revision, String path) throws IOException
  {
    InputStream input = null;
    String url =
      session.getUrlProvider().getRepositoryContentUrl(repository.getId(), path,
        revision);
    WebResource resource = session.getClient().resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      if (response.getStatus() != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        input = response.getEntityInputStream();
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return input;
  }

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   */
  @Override
  public List<FileObjectWrapper> getFiles(String revision, String path)
  {
    List<FileObjectWrapper> files = null;
    String url =
      session.getUrlProvider().getRepositoryBrowseUrl(repository.getId(), path,
        revision);
    WebResource resource = session.getClient().resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      if (response.getStatus() != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);

        BrowserResult result = response.getEntity(BrowserResult.class);

        AssertUtil.assertIsNotNull(result);
        files = new ArrayList<FileObjectWrapper>();

        List<FileObject> foList = result.getFiles();

        if (Util.isNotEmpty(foList))
        {
          for (FileObject fo : foList)
          {
            files.add(new FileObjectWrapper(this, revision, fo));
          }
        }
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return files;
  }

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
  @Override
  public List<FileObjectWrapper> getFiles(String revision)
  {
    return getFiles(revision, "");
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Repository repository;

  /** Field description */
  private JerseyClientSession session;
}
