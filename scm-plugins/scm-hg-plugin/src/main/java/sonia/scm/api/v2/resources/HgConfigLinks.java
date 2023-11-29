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
