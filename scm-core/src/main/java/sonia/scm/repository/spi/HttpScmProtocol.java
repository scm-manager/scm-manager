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

package sonia.scm.repository.spi;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.net.URI;

public abstract class HttpScmProtocol implements ScmProtocol {

  private final Repository repository;
  private final String basePath;

  public HttpScmProtocol(Repository repository, String basePath) {
    this.repository = repository;
    this.basePath = basePath;
  }

  @Override
  public String getType() {
    return "http";
  }

  @Override
  public String getUrl() {
    return URI.create(basePath + "/").resolve(String.format("repo/%s/%s", HttpUtil.encode(repository.getNamespace()), HttpUtil.encode(repository.getName()))).toASCIIString();
  }

  @Override
  public int getPriority() {
    return 150;
  }

  public final void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException {
    serve(request, response, repository, config);
  }

  protected abstract void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) throws ServletException, IOException;
}
