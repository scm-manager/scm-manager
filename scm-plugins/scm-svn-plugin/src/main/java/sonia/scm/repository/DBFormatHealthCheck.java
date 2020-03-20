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
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.plugin.Extension;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class DBFormatHealthCheck extends DirectoryHealthCheck
{

  /**
   * the logger for DBFormatHealthCheck
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DBFormatHealthCheck.class);

  /** Field description */
  private static final Set<String> INVALID_DBFORMAT = ImmutableSet.of("5");

  /** Field description */
  private static final HealthCheckFailure INCOMPATIBLE_DB_FORMAT =
    new HealthCheckFailure("AnOTx99ex1", "Incompatible DB Format",
      "https://bitbucket.org/sdorra/scm-manager/wiki/healthchecks/svn-incompatible-dbformat",
      "The subversion db format is incompatible with the svn version used within scm-manager.");

  /** Field description */
  private static final String DBFORMAT =
    "db".concat(File.separator).concat("format");

  /** Field description */
  private static final HealthCheckFailure COULD_NOT_READ_DB_FILE =
    new HealthCheckFailure("4IOTx8pvv1", "Could not read db/format file",
      "The db/format file of the repository was not readable.");

  /** Field description */
  private static final HealthCheckFailure COULD_NOT_OPEN_REPOSITORY =
    new HealthCheckFailure("6TOTx9RLD1", "Could not open svn repository",
      "The repository is not openable.");

  /** Field description */
  private static final HealthCheckFailure COULD_NOT_FIND_DB_FILE =
    new HealthCheckFailure("A9OTx8leC1", "Could not find db/format file",
      "The subversion repository does not contain the db/format file.");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  @Inject
  public DBFormatHealthCheck(RepositoryManager repositoryManager)
  {
    super(repositoryManager);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @return
   */
  @Override
  protected HealthCheckResult check(Repository repository, File directory)
  {
    List<HealthCheckFailure> failures = Lists.newArrayList();

    checkIfRepositoryIsOpenable(failures, repository, directory);
    checkForBadDBVersion(failures, repository, directory);

    return failures.isEmpty()
      ? HealthCheckResult.healthy()
      : HealthCheckResult.unhealthy(failures);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  protected boolean isCheckResponsible(Repository repository)
  {
    return SvnRepositoryHandler.TYPE_NAME.equalsIgnoreCase(
      repository.getType());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param failures
   * @param repository
   * @param directory
   */
  private void checkForBadDBVersion(List<HealthCheckFailure> failures,
    Repository repository, File directory)
  {
    File dbfile = new File(directory, DBFORMAT);

    if (dbfile.exists())
    {
      try
      {
        String content = Files.readFirstLine(dbfile, Charsets.US_ASCII);

        if ((content != null) && INVALID_DBFORMAT.contains(content.trim()))
        {
          failures.add(INCOMPATIBLE_DB_FORMAT);
        }
      }
      catch (IOException ex)
      {
        failures.add(COULD_NOT_READ_DB_FILE);
        logger.warn(
          "could not read db/format of ".concat(repository.getName()), ex);
      }
    }
    else
    {
      failures.add(COULD_NOT_FIND_DB_FILE);
      logger.warn("repository {} does not have a {} file",
        repository.getName(), DBFORMAT);
    }
  }

  /**
   * Method description
   *
   *
   * @param failures
   * @param repository
   * @param directory
   */
  private void checkIfRepositoryIsOpenable(List<HealthCheckFailure> failures,
    Repository repository, File directory)
  {
    SVNRepository svn = null;

    try
    {
      svn = SVNRepositoryFactory.create(SVNURL.fromFile(directory));
    }
    catch (SVNException ex)
    {

      failures.add(COULD_NOT_OPEN_REPOSITORY);

      logger.warn(
        "Could not open svn repository ".concat(repository.getName()), ex);
    }
    finally
    {
      SvnUtil.closeSession(svn);
    }
  }
}
