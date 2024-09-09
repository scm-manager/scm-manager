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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;

@Extension
@Enrich(Repository.class)
public class HgRepositoryConfigEnricher implements HalEnricher {

  private final HgConfigLinks links;

  @Inject
  public HgRepositoryConfigEnricher(HgConfigLinks links) {
    this.links = links;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (isHgRepository(repository)) {
      appender.appendLink("configuration", links.repository(repository).get());
    }
  }

  private boolean isHgRepository(Repository repository) {
    return HgRepositoryHandler.TYPE_NAME.equals(repository.getType());
  }
}
