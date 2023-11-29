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
