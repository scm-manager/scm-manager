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

import org.javahg.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;
import sonia.scm.web.HgUtil;


public class HgHookChangesetProvider implements HookChangesetProvider {

  private static final Logger LOG = LoggerFactory.getLogger(HgHookChangesetProvider.class);

  private final HgConfigResolver configResolver;
  private final HgRepositoryFactory factory;
  private final sonia.scm.repository.Repository scmRepository;
  private final String startRev;

  private HookChangesetResponse response;

  public HgHookChangesetProvider(HgConfigResolver configResolver, HgRepositoryFactory factory, sonia.scm.repository.Repository scmRepository, String startRev) {
    this.configResolver = configResolver;
    this.factory = factory;
    this.scmRepository = scmRepository;
    this.startRev = startRev;
  }

  @Override
  public synchronized HookChangesetResponse handleRequest(HookChangesetRequest request) {
    if (response == null) {
      Repository repository = null;

      try {
        repository = factory.openForRead(scmRepository);

        HgConfig config = configResolver.resolve(scmRepository);
        HgLogChangesetCommand cmd = HgLogChangesetCommand.on(repository, config);

        response = new HookChangesetResponse(
          cmd.rev(startRev.concat(":").concat(HgUtil.REVISION_TIP)).execute()
        );
      } catch (Exception ex) {
        LOG.error("could not retrieve changesets", ex);
      } finally {
        if (repository != null) {
          repository.close();
        }
      }
    }

    return response;
  }

}
