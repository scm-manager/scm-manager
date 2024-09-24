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

/**
 *
 * @deprecated
 */
@Deprecated
public class SvnImportHandler extends AbstactImportHandler
{

  public static final String SVN_DIR_LOCKS = "locks";

  private SvnRepositoryHandler handler;
 
  public SvnImportHandler(SvnRepositoryHandler handler)
  {
    this.handler = handler;
  }


  
  @Override
  protected String[] getDirectoryNames()
  {
    return new String[] { SVN_DIR_LOCKS };
  }

  
  @Override
  protected AbstractRepositoryHandler<?> getRepositoryHandler()
  {
    return handler;
  }

}
