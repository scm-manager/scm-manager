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
