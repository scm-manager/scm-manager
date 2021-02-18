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

import com.aragost.javahg.commands.LogCommand;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

class HgLazyChangesetResolver implements Callable<List<Changeset>> {

  private final HgRepositoryFactory factory;
  private final Repository repository;

  HgLazyChangesetResolver(HgRepositoryFactory factory, Repository repository) {
    this.factory = factory;
    this.repository = repository;
  }

  @Override
  public List<Changeset> call() {
    return LogCommand.on(factory.openForRead(repository)).execute().stream()
      .map(changeset -> new Changeset(
        changeset.getNode(),
        changeset.getTimestamp().getDate().getTime(),
        Person.toPerson(changeset.getUser()),
        changeset.getMessage())
      )
      .collect(Collectors.toList());
  }
}
