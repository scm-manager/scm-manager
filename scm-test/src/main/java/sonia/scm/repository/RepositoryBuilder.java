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
