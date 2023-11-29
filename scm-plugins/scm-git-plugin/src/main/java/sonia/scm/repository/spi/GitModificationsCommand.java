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
import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modifications;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


@Slf4j
public class GitModificationsCommand extends AbstractGitCommand implements ModificationsCommand {

  @Inject
  public GitModificationsCommand(@Assisted GitContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("java:S2093")
  public Modifications getModifications(String baseRevision, String revision) {
    try {
      return new ModificationsComputer(open()).compute(baseRevision, revision);
    } catch (IOException ex) {
      log.error("could not open repository: " + repository.getNamespaceAndName(), ex);
      throw new InternalRepositoryException(entity(repository), "could not open repository: " + repository.getNamespaceAndName(), ex);
    }
  }


  @Override
  public Modifications getModifications(String revision) {
    return getModifications(null, revision);
  }

  public interface Factory {
    ModificationsCommand create(GitContext context);
  }

}
