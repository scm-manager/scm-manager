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

/**
 * @deprecated
 */
@Deprecated
public class GitImportHandler extends AbstactImportHandler {

  
  public static final String GIT_DIR = ".git";

  
  public static final String GIT_DIR_REFS = "refs";

 
  private static final Logger logger =
    LoggerFactory.getLogger(GitImportHandler.class);

  private GitRepositoryHandler handler;

  public GitImportHandler(GitRepositoryHandler handler) {
    this.handler = handler;
  }



  @Override
  protected String[] getDirectoryNames() {
    return new String[]{GIT_DIR, GIT_DIR_REFS};
  }


  @Override
  protected AbstractRepositoryHandler<?> getRepositoryHandler() {
    return handler;
  }

}
