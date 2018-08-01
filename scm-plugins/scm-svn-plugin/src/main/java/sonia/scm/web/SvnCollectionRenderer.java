/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.server.dav.CollectionRenderer;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceURI;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringWriter;

import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnCollectionRenderer implements CollectionRenderer
{

  /** Field description */
  private static final String RESOURCE_SVNINDEX =
    "/sonia/scm/svn.index.mustache";

  /**
   * the logger for SvnCollectionRenderer
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnCollectionRenderer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param configuration
   * @param templateEngineFactory
   * @param repositoryProvider
   * @param requestProvider
   */
  @Inject
  public SvnCollectionRenderer(ScmConfiguration configuration,
    TemplateEngineFactory templateEngineFactory,
    RepositoryProvider repositoryProvider,
    Provider<HttpServletRequest> requestProvider)
  {
    this.configuration = configuration;
    this.templateEngineFactory = templateEngineFactory;
    this.repositoryProvider = repositoryProvider;
    this.requestProvider = requestProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param buffer
   * @param resource
   *
   * @throws SVNException
   */
  @Override
  public void renderCollection(StringBuilder buffer, DAVResource resource)
    throws SVNException
  {
    TemplateEngine engine = templateEngineFactory.getDefaultEngine();

    StringWriter writer = new StringWriter();

    try
    {
      Template template = engine.getTemplate(RESOURCE_SVNINDEX);

      template.execute(writer, createRepositoryWrapper(resource));
    }
    catch (IOException ex)
    {
      logger.error("could not render directory", ex);

      throw new SVNException(SVNErrorMessage.UNKNOWN_ERROR_MESSAGE, ex);
    }

    writer.flush();
    buffer.append(writer.toString());
  }

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   *
   * @throws SVNException
   */
  private RepositoryWrapper createRepositoryWrapper(DAVResource resource)
    throws SVNException
  {
    Builder<DirectoryEntry> entries = ImmutableList.builder();

    DAVResourceURI uri = resource.getResourceURI();
    String path = uri.getPath();

    if (!HttpUtil.SEPARATOR_PATH.equals(path))
    {
      String completePath = HttpUtil.append(uri.getContext(), path);
      String parent = DAVPathUtil.removeTail(completePath, true);

      entries.add(new DirectoryEntry("..", parent, true));
    }

    for (Iterator iterator = resource.getEntries().iterator();
      iterator.hasNext(); )
    {
      SVNDirEntry entry = (SVNDirEntry) iterator.next();

      entries.add(new DirectoryEntry(resource, entry));
    }

    //J-
    return new RepositoryWrapper(
      repositoryProvider.get(), 
      resource,
      new DirectoryOrdering().immutableSortedCopy(entries.build())
    );
    //J+
  }

  private String getBaseUrl() {
    return HttpUtil.getCompleteUrl(requestProvider.get());
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/11/10
   * @author         Enter your name here...
   */
  private static class DirectoryEntry
  {

    /**
     * Constructs ...
     *
     *
     * @param resource
     * @param entry
     */
    public DirectoryEntry(DAVResource resource, SVNDirEntry entry)
    {
      this.name = entry.getName();
      this.url = createUrl(resource, entry);
      this.directory = entry.getKind() == SVNNodeKind.DIR;
    }

    /**
     * Constructs ...
     *
     *
     * @param name
     * @param url
     * @param directory
     */
    public DirectoryEntry(String name, String url, boolean directory)
    {
      this.name = name;
      this.url = url;
      this.directory = directory;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getName()
    {
      return name;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getUrl()
    {
      return url;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public boolean isDirectory()
    {
      return directory;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param resource
     * @param entry
     *
     * @return
     */
    private String createUrl(DAVResource resource, SVNDirEntry entry)
    {
      StringBuilder buffer = new StringBuilder();

      buffer.append(resource.getResourceURI().getContext());

      String path = resource.getResourceURI().getPath();

      if (!HttpUtil.SEPARATOR_PATH.equals(path))
      {
        buffer.append(path);
      }

      buffer.append(DAVPathUtil.standardize(entry.getName()));

      if (isDirectory())
      {
        buffer.append(HttpUtil.SEPARATOR_PATH);
      }

      return buffer.toString();
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final boolean directory;

    /** Field description */
    private final String name;

    /** Field description */
    private final String url;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/11/10
   * @author         Enter your name here...
   */
  private static class DirectoryOrdering extends Ordering<DirectoryEntry>
  {

    /**
     * Method description
     *
     *
     * @param left
     * @param right
     *
     * @return
     */
    @Override
    public int compare(DirectoryEntry left, DirectoryEntry right)
    {
      int result;

      if (left.isDirectory() &&!right.isDirectory())
      {
        result = -1;
      }
      else if (!left.isDirectory() && right.isDirectory())
      {
        result = 1;
      }
      else
      {
        if ("..".equals(left.getName()))
        {
          result = -1;
        }
        else if ("..".equals(right.getName()))
        {
          result = 1;
        }
        else
        {
          result = left.getName().compareTo(right.getName());
        }
      }

      return result;
    }
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/11/10
   * @author         Enter your name here...
   */
  private static class RepositoryWrapper
  {

    /**
     * Constructs ...
     *
     *
     *
     * @param repository
     * @param resource
     * @param entries
     */
    public RepositoryWrapper(Repository repository, DAVResource resource, List<DirectoryEntry> entries)
    {
      this.repository = repository;
      this.resource = resource;
      this.entries = entries;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public List<DirectoryEntry> getEntries()
    {
      return entries;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getName()
    {
      return repository.getName();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Repository getRepository()
    {
      return repository;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final List<DirectoryEntry> entries;

    /** Field description */
    private final Repository repository;

    /** Field description */
    private final DAVResource resource;
  }


  //~--- fields ---------------------------------------------------------------

  private final Provider<HttpServletRequest> requestProvider;
  
  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final RepositoryProvider repositoryProvider;

  /** Field description */
  private final TemplateEngineFactory templateEngineFactory;
}
