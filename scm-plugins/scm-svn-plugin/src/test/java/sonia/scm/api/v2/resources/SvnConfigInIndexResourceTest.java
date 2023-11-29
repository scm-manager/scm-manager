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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.util.Providers;
import jakarta.ws.rs.core.MediaType;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini")
public class SvnConfigInIndexResourceTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ObjectNode root = objectMapper.createObjectNode();
  private final SvnConfigInIndexResource svnConfigInIndexResource;

  public SvnConfigInIndexResourceTest() {
    root.put("_links", objectMapper.createObjectNode());
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    svnConfigInIndexResource = new SvnConfigInIndexResource(Providers.of(pathInfoStore), objectMapper);
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void admin() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    svnConfigInIndexResource.enrich(context);

    assertEquals("/v2/config/svn", root.get("_links").get("svnConfig").get("href").asText());
  }

  @Test
  @SubjectAware(username = "readOnly", password = "secret")
  public void user() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    svnConfigInIndexResource.enrich(context);

    assertTrue(root.get("_links").iterator().hasNext());
  }

  @Test
  public void anonymous() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    svnConfigInIndexResource.enrich(context);

    assertFalse(root.get("_links").iterator().hasNext());
  }
}
