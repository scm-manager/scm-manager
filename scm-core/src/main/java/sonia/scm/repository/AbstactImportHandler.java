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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.ImportResult.Builder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Abstract base class for directory based {@link ImportHandler} and
 * {@link AdvancedImportHandler}.
 *
 * @since 1.12
 * @deprecated
 */
@Deprecated
public abstract class AbstactImportHandler implements AdvancedImportHandler
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(AbstactImportHandler.class);


  protected abstract String[] getDirectoryNames();

  protected abstract AbstractRepositoryHandler<?> getRepositoryHandler();


 
  @Override
  public List<String> importRepositories(RepositoryManager manager) throws IOException {
    return doRepositoryImport(manager, true).getImportedDirectories();
  }

 
  @Override
  public ImportResult importRepositoriesFromDirectory(RepositoryManager manager)
  {
    return doRepositoryImport(manager, false);
  }

  protected Repository createRepository(File repositoryDirectory, String repositoryName) throws IOException {
    Repository repository = new Repository();

    repository.setName(repositoryName);
    repository.setType(getTypeName());

    return repository;
  }

  /**
   * Repository import.
   *
   *
   * @param manager repository manager
   * @param throwExceptions true to throw exception
   *
   * @return import result
   *
   * @throws IOException
   */
  private ImportResult doRepositoryImport(RepositoryManager manager, boolean throwExceptions) {
    Builder builder = ImportResult.builder();

    logger.trace("search for repositories to import");

    // TODO #8783
//    try
//    {
//
//      List<String> repositoryNames =
//        RepositoryUtil.getRepositoryNames(getRepositoryHandler(),
//          getDirectoryNames());
//
//      for (String repositoryName : repositoryNames)
//      {
//        importRepository(manager, builder, throwExceptions, repositoryName);
//      }
//
//    }
//    catch (IOException ex)
//    {
//      handleException(ex, throwExceptions);
//    }

    return builder.build();
  }

  private <T extends Exception> void handleException(T ex,
    boolean throwExceptions)
    throws T
  {
    logger.warn("error durring repository directory import", ex);

    if (throwExceptions)
    {
      throw ex;
    }
  }

  private void importRepository(RepositoryManager manager, Builder builder,
    boolean throwExceptions, String directoryName)
    throws IOException
  {
    logger.trace("check repository {} for import", directoryName);

    // TODO #8783
//
//    Repository repository = manager.get(namespaceAndName);
//
//    if (repository == null)
//    {
//      try
//      {
//        importRepository(manager, repositoryName);
//        builder.addImportedDirectory(repositoryName);
//      }
//      catch (IOException ex)
//      {
//        builder.addFailedDirectory(repositoryName);
//        handleException(ex, throwExceptions);
//      }
//      catch (IllegalStateException ex)
//      {
//        builder.addFailedDirectory(repositoryName);
//        handleException(ex, throwExceptions);
//      }
//      catch (RepositoryException ex)
//      {
//        builder.addFailedDirectory(repositoryName);
//        handleException(ex, throwExceptions);
//      }
//    }
//    else if (logger.isDebugEnabled())
//    {
//      logger.debug("repository {} is already managed", repositoryName);
//    }
  }





  private String getTypeName()
  {
    return getRepositoryHandler().getType().getName();
  }
}
