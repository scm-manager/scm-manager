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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class HgGlobalConfigInIndexResource extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public HgGlobalConfigInIndexResource(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read(HgGlobalConfig.PERMISSION).isPermitted()) {
      String hgConfigUrl = new LinkBuilder(scmPathInfoStore.get().get(), HgConfigResource.class)
        .method("get")
        .parameters()
        .href();

      JsonNode hgConfigRefNode = createObject(singletonMap("href", value(hgConfigUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "hgConfig", hgConfigRefNode);
    }
  }
}
