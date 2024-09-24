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

package sonia.scm.legacy;

import com.google.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;

@Extension
@Enrich(Index.class)
public class LegacyIndexHalEnricher implements HalEnricher {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public LegacyIndexHalEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  private String createLink() {
    return new LinkBuilder(scmPathInfoStoreProvider.get().get(), LegacyRepositoryService.class)
      .method("getNameAndNamespaceForRepositoryId")
      .parameters("REPOID")
      .href()
      .replace("REPOID", "{id}");
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    appender.appendLink("nameAndNamespace", createLink());
  }
}
