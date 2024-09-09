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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.util.Providers;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.repository.Repository;

public class HgConfigLinks {

  private final Provider<ScmPathInfoStore> pathInfoStore;

  @Inject
  public HgConfigLinks(Provider<ScmPathInfoStore> pathInfoStore) {
    this.pathInfoStore = pathInfoStore;
  }

  @VisibleForTesting
  public HgConfigLinks(ScmPathInfoStore pathInfoStore) {
    this.pathInfoStore = Providers.of(pathInfoStore);
  }


  public GlobalConfigLinks global() {
    LinkBuilder linkBuilder = new LinkBuilder(pathInfoStore.get().get(), HgConfigResource.class);
    return new GlobalConfigLinks() {
      @Override
      public String get() {
        return linkBuilder.method("get").parameters().href();
      }

      @Override
      public String update() {
        return linkBuilder.method("update").parameters().href();
      }

      @Override
      public String autoConfigure() {
        LinkBuilder linkBuilder = new LinkBuilder(pathInfoStore.get().get(), HgGlobalConfigAutoConfigurationResource.class);
        return linkBuilder.method("autoConfiguration").parameters().href();
      }
    };
  }

  public ConfigLinks repository(Repository repository) {
    return repository(repository.getNamespace(), repository.getName());
  }

  public ConfigLinks repository(String namespace, String name) {
    LinkBuilder linkBuilder = new LinkBuilder(pathInfoStore.get().get(), HgConfigResource.class, HgRepositoryConfigResource.class)
      .method("getRepositoryConfigResource")
      .parameters();

    return new ConfigLinks() {
      @Override
      public String get() {
        return linkBuilder.method("getHgRepositoryConfig").parameters(namespace, name).href();
      }

      @Override
      public String update() {
        return linkBuilder.method("updateHgRepositoryConfig").parameters(namespace, name).href();
      }
    };
  }

  public interface ConfigLinks  {
    String get();
    String update();
  }

  public interface GlobalConfigLinks extends ConfigLinks  {
    String autoConfigure();
  }
}
