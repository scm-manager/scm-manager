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
public class GitConfigInIndexResourceTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ObjectNode root = objectMapper.createObjectNode();
  private final GitConfigInIndexResource gitConfigInIndexResource;

  public GitConfigInIndexResourceTest() {
    root.put("_links", objectMapper.createObjectNode());
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    gitConfigInIndexResource = new GitConfigInIndexResource(Providers.of(pathInfoStore), objectMapper);
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void admin() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    gitConfigInIndexResource.enrich(context);

    assertEquals("/v2/config/git", root.get("_links").get("gitConfig").get("href").asText());
  }

  @Test
  @SubjectAware(username = "readOnly", password = "secret")
  public void user() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    gitConfigInIndexResource.enrich(context);

    assertTrue(root.get("_links").iterator().hasNext());
  }

  @Test
  public void anonymous() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    gitConfigInIndexResource.enrich(context);

    assertFalse(root.get("_links").iterator().hasNext());
  }
}
