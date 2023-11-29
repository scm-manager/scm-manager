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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;

import java.util.Base64;

import static sonia.scm.it.webapp.IntegrationTestUtil.createClient;

public class ScmClient {
  private final String user;
  private final String password;

  private final Client client;

  public static ScmClient anonymous() {
    return new ScmClient(null, null);
  }

  public ScmClient(String user, String password) {
    this.user = user;
    this.password = password;
    this.client = createClient();
  }

  public Invocation.Builder resource(String url) {
    if (user == null) {
      return client.target(url).request();
    } else {
      return client.target(url).request().header("Authorization", createAuthHeaderValue());
    }
  }

  public Invocation.Builder resource(String url, MediaType mediaType) {
    if (user == null) {
      return client.target(url).request(mediaType);
    } else {
      return client.target(url).request(mediaType).header("Authorization", createAuthHeaderValue());
    }
  }

  public String createAuthHeaderValue() {
    return "Basic " + Base64.getEncoder().encodeToString((user +":"+ password).getBytes());
  }
}
