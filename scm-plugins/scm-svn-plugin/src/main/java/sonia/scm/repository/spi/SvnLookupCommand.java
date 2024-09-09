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

package sonia.scm.repository.spi;

import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.Optional;

@Slf4j
public class SvnLookupCommand extends AbstractSvnCommand implements LookupCommand {

  protected SvnLookupCommand(SvnContext context) {
    super(context);
  }

  @Override
  public <T> Optional<T> lookup(LookupCommandRequest<T> request) {
    try {
      if (request.getArgs().length > 1 && "propget".equalsIgnoreCase(request.getArgs()[0])) {
        return lookupProps(request);
      }
    } catch (SVNException e) {
      log.error("Lookup failed: ", e);
    }

    return Optional.empty();
  }

  private <T> Optional<T> lookupProps(LookupCommandRequest<T> request) throws SVNException {
    if (request.getArgs()[1].equalsIgnoreCase("uuid")) {
      if (!request.getType().equals(String.class)) {
        throw new IllegalArgumentException("uuid can only be returned as String");
      }
      SVNRepository repository = context.open();
      return Optional.of((T) repository.getRepositoryUUID(true));
    }
    log.debug("No result found on lookup");
    return Optional.empty();
  }
}
