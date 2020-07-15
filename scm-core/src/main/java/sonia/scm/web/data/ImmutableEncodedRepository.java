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
package sonia.scm.web.data;

import sonia.scm.repository.Repository;

public final class ImmutableEncodedRepository {

  private final Repository repository;

  public ImmutableEncodedRepository(Repository repository) {
    this.repository = repository;
  }

  public String getContact() {
    return Encoder.encode(repository.getContact());
  }

  public Long getCreationDate() {
    return repository.getCreationDate();
  }

  public String getDescription() {
    return Encoder.encode(repository.getDescription());
  }

  public String getId() {
    return repository.getId();
  }

  public Long getLastModified() {
    return repository.getLastModified();
  }

  public String getName() {
    return repository.getName();
  }

  public String getType() {
    return repository.getType();
  }

}
