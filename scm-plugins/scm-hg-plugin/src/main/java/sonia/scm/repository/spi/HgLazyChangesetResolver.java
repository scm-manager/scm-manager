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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.javahg.commands.LogCommand;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;

import java.util.Iterator;
import java.util.concurrent.Callable;

public class HgLazyChangesetResolver implements Callable<Iterable<Changeset>> {

  private final HgRepositoryFactory factory;
  private final Repository repository;

  @Inject
  HgLazyChangesetResolver(HgRepositoryFactory factory, @Assisted HgCommandContext context) {
    this.factory = factory;
    this.repository = context.getScmRepository();
  }

  @Override
  public Iterable<Changeset> call() {
    Iterator<Changeset> iterator = LogCommand.on(factory.openForRead(repository)).execute().stream()
      .map(changeset -> new Changeset(
        changeset.getNode(),
        changeset.getTimestamp().getDate().getTime(),
        Person.toPerson(changeset.getUser()),
        changeset.getMessage())
      )
      .iterator();
    return () -> iterator;
  }

  public interface Factory {
    HgLazyChangesetResolver create(HgCommandContext context);
  }

}
