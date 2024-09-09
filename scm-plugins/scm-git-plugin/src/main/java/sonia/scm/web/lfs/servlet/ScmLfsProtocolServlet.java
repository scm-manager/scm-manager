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

package sonia.scm.web.lfs.servlet;

import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;

/**
 * Provides an implementation for the git-lfs Batch API.
 *
 * @since 1.54
 * Created by omilke on 11.05.2017.
 */
public class ScmLfsProtocolServlet extends LfsProtocolServlet {

  private final LargeFileRepository repository;

  public ScmLfsProtocolServlet(LargeFileRepository largeFileRepository) {
    this.repository = largeFileRepository;
  }


  @Override
  protected LargeFileRepository getLargeFileRepository(LfsRequest request, String path, String auth)  throws LfsException {
    return repository;
  }
}
