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
package sonia.scm.repository;

public class RepositoryBuilder {

  private String id = "id-" + ++nextID;
  private String contact = "test@example.com";
  private String description = "";
  private String namespace = "test";
  private String name = "name";
  private String type = "git";

  private static int nextID = 0;

  public RepositoryBuilder type(String type) {
    this.type = type;
    return this;
  }

  public RepositoryBuilder contact(String contact) {
    this.contact = contact;
    return this;
  }

  public RepositoryBuilder namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  public RepositoryBuilder name(String name) {
    this.name = name;
    return this;
  }

  public RepositoryBuilder description(String description) {
    this.description = description;
    return this;
  }

  public Repository build() {
    Repository repository = new Repository();
    repository.setId(id);
    repository.setType(type);
    repository.setContact(contact);
    repository.setNamespace(namespace);
    repository.setName(name);
    repository.setDescription(description);
    return repository;
  }
}
