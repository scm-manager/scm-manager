/**
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
package sonia.scm.api.rest.resources;

import sonia.scm.ModelObject;

public class Simple implements ModelObject {

  private String id;
  private String data;

  public Simple(String id, String data) {
    this.id = id;
    this.data = data;
  }

  public String getData() {
    return data;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setLastModified(Long timestamp) {

  }

  @Override
  public Long getCreationDate() {
    return null;
  }

  @Override
  public void setCreationDate(Long timestamp) {

  }

  @Override
  public Long getLastModified() {
    return null;
  }

  @Override
  public String getType() {
    return null;
  }
  @Override
  public boolean isValid() {
    return false;
  }
}
