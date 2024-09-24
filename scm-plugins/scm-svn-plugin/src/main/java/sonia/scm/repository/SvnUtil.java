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

package sonia.scm.repository;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.repository.ConsolidatingModificationCollector.consolidate;


public final class SvnUtil
{

  public static final String XML_CONTENT_TYPE = "text/xml; charset=\"utf-8\"";

  private static final String ID_TRANSACTION_PREFIX = "-1:";

  /**
   * svn path updated
   * same as modified ({@link SVNLogEntryPath#TYPE_MODIFIED})?
   */
  private static final char TYPE_UPDATED = 'U';

  private static final String USERAGENT_SVN = "svn/";

 
  private static final Logger logger = LoggerFactory.getLogger(SvnUtil.class);

  private static final String ID_TRANSACTION_PATTERN =
    ID_TRANSACTION_PREFIX.concat("%s");


  private SvnUtil() {}


  public static long parseRevision(String v, Repository repository) {
    long result = -1l;

    if (!Strings.isNullOrEmpty(v))
    {
      try
      {
        result = Long.parseLong(v);
      }
      catch (NumberFormatException ex)
      {
        throw notFound(entity("Revision", v).in(repository));
      }
    }

    return result;
  }

  public static Modifications createModifications(String startRevision, String endRevision, Collection<SVNLogEntry> entries) {
    Collection<Modification> consolidatedModifications =
      entries.stream()
        .flatMap(SvnUtil::createModificationStream)
        .collect(consolidate());
    return new Modifications(startRevision, endRevision, consolidatedModifications);
  }

  public static Modifications createModifications(SVNLogEntry entry, String revision) {
    return new Modifications(revision, createModificationStream(entry).collect(toList()));
  }

  private static Stream<Modification> createModificationStream(SVNLogEntry entry) {
    Map<String, SVNLogEntryPath> changeMap = entry.getChangedPaths();

    if (Util.isNotEmpty(changeMap)) {
      return changeMap.values().stream()
        .map(e -> asModification(e.getType(), e.getPath()))
        .filter(Optional::isPresent)
        .map(Optional::get);
    } else {
      return Stream.empty();
    }
  }

  public static Optional<Modification> asModification(char type, String path) {
    if (path.startsWith("/"))
    {
      path = path.substring(1);
    }

    switch (type)
    {
      case SVNLogEntryPath.TYPE_ADDED :
        return Optional.of(new Added(path));

      case SVNLogEntryPath.TYPE_DELETED :
        return Optional.of(new Removed(path));

      case TYPE_UPDATED :
      case SVNLogEntryPath.TYPE_MODIFIED :
        return Optional.of(new Modified(path));

      default :
        logger.debug("unknown modification type {}", type);
        return empty();
    }
  }


  public static void closeSession(SVNRepository repository)
  {
    if (repository != null)
    {
      try
      {
        repository.closeSession();
      }
      catch (Exception ex)
      {
        logger.error("could not close svn repository session", ex);
      }
    }
  }


  @SuppressWarnings("unchecked")
  public static Changeset createChangeset(SVNLogEntry entry)
  {
    long revision = entry.getRevision();
    Changeset changeset = new Changeset(String.valueOf(revision),
                            entry.getDate().getTime(),
                            Person.toPerson(entry.getAuthor()),
                            entry.getMessage());

    if (revision > 1)
    {
      changeset.getParents().add(String.valueOf(revision - 1));
    }
    return changeset;
  }


  public static List<Changeset> createChangesets(List<SVNLogEntry> entries)
  {
    List<Changeset> changesets = Lists.newArrayList();

    for (SVNLogEntry entry : entries)
    {
      changesets.add(createChangeset(entry));
    }

    return changesets;
  }

  @SuppressWarnings("java:S1149") // we can not use StringBuild SVNXMLUtil requires StringBuffer
  public static String createErrorBody(SVNErrorCode errorCode)
  {
    StringBuffer xmlBuffer = new StringBuffer();

    SVNXMLUtil.addXMLHeader(xmlBuffer);

    List<String> namespaces = Lists.newArrayList(DAVElement.DAV_NAMESPACE,
                                DAVElement.SVN_APACHE_PROPERTY_NAMESPACE);

    SVNXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
      DAVXMLUtil.SVN_DAV_ERROR_TAG, namespaces, SVNXMLUtil.PREFIX_MAP,
      xmlBuffer);

    SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_APACHE_PROPERTY_PREFIX,
      "human-readable", SVNXMLUtil.XML_STYLE_NORMAL, "errcode",
      String.valueOf(errorCode.getCode()), xmlBuffer);
    xmlBuffer.append(
      SVNEncodingUtil.xmlEncodeCDATA(errorCode.getDescription()));
    SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_APACHE_PROPERTY_PREFIX,
      "human-readable", xmlBuffer);
    SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
      DAVXMLUtil.SVN_DAV_ERROR_TAG, xmlBuffer);

    return xmlBuffer.toString();
  }


  public static String createTransactionEntryId(String transaction)
  {
    return String.format(ID_TRANSACTION_PATTERN, transaction);
  }


  public static void dispose(SVNClientManager clientManager)
  {
    if (clientManager != null)
    {
      try
      {
        clientManager.dispose();
      }
      catch (Exception ex)
      {
        logger.error("could not dispose clientmanager", ex);
      }
    }
  }

  public static void sendError(HttpServletRequest request,
    HttpServletResponse response, int statusCode, SVNErrorCode errorCode)
    throws IOException
  {
    HttpUtil.drainBody(request);

    response.setStatus(statusCode);
    response.setContentType(XML_CONTENT_TYPE);

    PrintWriter writer = null;

    try
    {
      writer = response.getWriter();
      writer.println(createErrorBody(errorCode));
    }
    finally
    {
      Closeables.close(writer, true);
    }
  }

  public static long getRevisionNumber(String revision, Repository repository) {
    // REVIEW Bei SVN wird ohne Revision die -1 genommen, was zu einem Fehler führt
    long revisionNumber = -1;

    if (Util.isNotEmpty(revision))
    {
      try
      {
        revisionNumber = Long.parseLong(revision);
      }
      catch (NumberFormatException ex)
      {
        throw notFound(entity("Revision", revision).in(repository));
      }
    }

    return revisionNumber;
  }


  public static String getTransactionId(String id)
  {
    return id.substring(ID_TRANSACTION_PREFIX.length());
  }


  public static boolean isSvnClient(HttpServletRequest request)
  {
    return HttpUtil.userAgentStartsWith(request, USERAGENT_SVN);
  }


  public static boolean isTransactionEntryId(String id)
  {
    return Strings.nullToEmpty(id).startsWith(ID_TRANSACTION_PREFIX);
  }

}
