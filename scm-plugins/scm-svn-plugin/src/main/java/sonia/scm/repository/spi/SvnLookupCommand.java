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
  public <T> Optional<T> lookup(LookupCommandRequest request) {
    try {
      if (request.getArgs().length > 1 && "propget".equalsIgnoreCase(request.getArgs()[0])) {
        return lookupProps(request);
      }
    } catch (SVNException e) {
      log.error("Lookup failed: ", e);
    }

    return Optional.empty();
  }

  private <T> Optional<T> lookupProps(LookupCommandRequest request) throws SVNException {
    if (request.getArgs()[1].equalsIgnoreCase("uuid")) {
      SVNRepository repository = context.open();
      return Optional.of((T) repository.getRepositoryUUID(true));
    }
    log.debug("No result found on lookup");
    return Optional.empty();
  }
}
