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
