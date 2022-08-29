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

package sonia.scm.repository.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ChangesetsCommand;
import sonia.scm.repository.spi.ChangesetsCommandRequest;

import java.util.Optional;

public class ChangesetsCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ChangesetsCommandBuilder.class);

  private final Repository repository;
  private final ChangesetsCommand changesetsCommand;

  private final ChangesetsCommandRequest request = new ChangesetsCommandRequest();

  public ChangesetsCommandBuilder( Repository repository, ChangesetsCommand changesetsCommand) {
    this.repository = repository;
    this.changesetsCommand = changesetsCommand;
  }

  public Iterable<Changeset> getChangesets() {
      LOG.debug("Retrieve all changesets from {{}}", repository);
      return changesetsCommand.getChangesets(request);
  }

  public Optional<Changeset> getLatestChangeset() {
    LOG.debug("Retrieve latest changeset from {{}}", repository);
    return changesetsCommand.getLatestChangeset();
  }
}
