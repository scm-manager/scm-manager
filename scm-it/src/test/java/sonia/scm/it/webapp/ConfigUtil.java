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

package sonia.scm.it.webapp;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.junit.Assert;
import sonia.scm.api.v2.resources.ConfigDto;
import sonia.scm.web.VndMediaType;

import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;

public class ConfigUtil {

  public static ConfigDto readConfig(ScmClient client) {
    Invocation.Builder wr = createResource(client, "config");
    Response response = wr.buildGet().invoke();

    assertNotNull(response);
    Assert.assertEquals(200, response.getStatus());

    ConfigDto config = response.readEntity(ConfigDto.class);

    response.close();
    assertNotNull(config);
    return config;
  }

  public static void writeConfig(ScmClient client, ConfigDto config) {
    Response response =
      createResource(client, "config", VndMediaType.CONFIG)
        .put(Entity.entity(config, VndMediaType.CONFIG));

    Assert.assertEquals(204, response.getStatus());
    response.close();
  }
}
