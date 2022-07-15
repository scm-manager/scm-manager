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

package sonia.scm.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Sebastian Sdorra
 */
public class SvnCollectionRenderer implements CollectionRenderer {

  private final TemplateEngineFactory templateEngineFactory;
  private final RepositoryProvider repositoryProvider;
  private static final String RESOURCE_SVNINDEX = "/sonia/scm/svn.index.mustache";

  private static final Logger LOG = LoggerFactory.getLogger(SvnCollectionRenderer.class);


  @Inject
  public SvnCollectionRenderer(TemplateEngineFactory templateEngineFactory,
                               RepositoryProvider repositoryProvider) {
    this.templateEngineFactory = templateEngineFactory;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  @SuppressWarnings("java:S2139")
  public void renderCollection(StringBuilder buffer, DAVResource resource)
    throws SVNException {
    TemplateEngine engine = templateEngineFactory.getDefaultEngine();

    StringWriter writer = new StringWriter();

    try {
      Template template = engine.getTemplate(RESOURCE_SVNINDEX);

      template.execute(writer, createRepositoryWrapper(resource));
    } catch (IOException ex) {
      LOG.error("could not render directory", ex);

      throw new SVNException(SVNErrorMessage.UNKNOWN_ERROR_MESSAGE, ex);
    }

    writer.flush();
    buffer.append(writer);
  }

  private RepositoryWrapper createRepositoryWrapper(DAVResource resource)
    throws SVNException {
    Builder<DirectoryEntry> entries = ImmutableList.builder();

    DAVResourceURI uri = resource.getResourceURI();
    String path = uri.getPath();

    if (!HttpUtil.SEPARATOR_PATH.equals(path)) {
      String completePath = HttpUtil.append(uri.getContext(), path);
      String parent = DAVPathUtil.removeTail(completePath, true);

      entries.add(new DirectoryEntry("..", parent, true));
    }

    for (Object o : resource.getEntries()) {
      SVNDirEntry entry = (SVNDirEntry) o;

      entries.add(new DirectoryEntry(resource, entry));
    }

    //J-
    return new RepositoryWrapper(
      repositoryProvider.get(),
      new DirectoryOrdering().immutableSortedCopy(entries.build())
    );
    //J+
  }
  private static class DirectoryEntry {
    private final boolean directory;
    private final String name;
    private final String url;


    public DirectoryEntry(DAVResource resource, SVNDirEntry entry) {
      this.name = entry.getName();
      this.url = createUrl(resource, entry);
      this.directory = entry.getKind() == SVNNodeKind.DIR;
    }

    public DirectoryEntry(String name, String url, boolean directory) {
      this.name = name;
      this.url = url;
      this.directory = directory;
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public boolean isDirectory() {
      return directory;
    }
    private String createUrl(DAVResource resource, SVNDirEntry entry) {
      StringBuilder buffer = new StringBuilder();

      buffer.append(resource.getResourceURI().getContext());

      String path = resource.getResourceURI().getPath();

      if (!HttpUtil.SEPARATOR_PATH.equals(path)) {
        buffer.append(path);
      }

      buffer.append(DAVPathUtil.standardize(entry.getName()));

      if (isDirectory()) {
        buffer.append(HttpUtil.SEPARATOR_PATH);
      }

      return buffer.toString();
    }

  }
  private static class DirectoryOrdering extends Ordering<DirectoryEntry> {

    @Override
    public int compare(DirectoryEntry left, DirectoryEntry right) {
      int result = 0;

      if (left == null || right == null) {
        return result;
      }

      if (left.isDirectory() && !right.isDirectory()) {
        result = -1;
      } else if (!left.isDirectory() && right.isDirectory()) {
        result = 1;
      } else {
        if ("..".equals(left.getName())) {
          result = -1;
        } else if ("..".equals(right.getName())) {
          result = 1;
        } else {
          result = left.getName().compareTo(right.getName());
        }
      }

      return result;
    }

  }

  private static class RepositoryWrapper {
    private final List<DirectoryEntry> entries;

    private final Repository repository;


    public RepositoryWrapper(Repository repository, List<DirectoryEntry> entries) {
      this.repository = repository;
      this.entries = entries;
    }

    public List<DirectoryEntry> getEntries() {
      return entries;
    }

    public String getName() {
      return repository.getName();
    }
    public Repository getRepository() {
      return repository;
    }
  }
}
