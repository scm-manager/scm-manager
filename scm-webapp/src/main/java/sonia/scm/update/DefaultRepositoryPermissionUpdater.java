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

package sonia.scm.update;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.migration.UpdateException;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DefaultRepositoryPermissionUpdater implements RepositoryPermissionUpdater {

  private final RepositoryLocationResolver locationResolver;
  private final DataStore<Namespace> namespaceStore;
  private final JAXBContext jaxbRepositoryContext;

  @Inject
  public DefaultRepositoryPermissionUpdater(RepositoryLocationResolver locationResolver,
                                            DataStoreFactory dataStoreFactory) {
    this.locationResolver = locationResolver;
    this.namespaceStore = dataStoreFactory.withType(Namespace.class).withName("namespaces").build();
    this.jaxbRepositoryContext = createJAXBContext();
  }

  private JAXBContext createJAXBContext() {
    try {
      return JAXBContext.newInstance(Repository.class);
    } catch (JAXBException e) {
      throw new UpdateException("could not create Repository XML marshaller", e);
    }
  }

  @Override
  public void removePermission(RepositoryPermissionHolder permissionHolder, String permissionName) {
    List<RepositoryPermission> permissionsToUpdate = permissionHolder
      .getPermissions()
      .stream()
      .filter(permission -> shouldPermissionBeRemoved(permission, permissionName))
      .toList();
    permissionsToUpdate.forEach(permissionHolder::removePermission);

    List<RepositoryPermission> updatedPermissions = permissionsToUpdate
      .stream()
      .map(permission -> removePermissionFromVerbs(permission, permissionName))
      .toList();
    updatedPermissions.forEach(permission -> {

      if (permissionHolder instanceof Repository repository) {
        log.debug(
          "removing permission {} from {} inside repository {}",
          permissionName,
          permission.getName(),
          repository
        );
      }

      if (permissionHolder instanceof Namespace namespace) {
        log.debug(
          "removing permission {} from {} inside namespace {}",
          permissionName,
          permission.getName(),
          namespace.getNamespace());
      }

      permissionHolder.addPermission(permission);
    });

    if (permissionHolder instanceof Repository repository) {
      Path path = locationResolver.forClass(Path.class).getLocation(repository.getId());
      this.writeRepository(repository, path);
    }

    if (permissionHolder instanceof Namespace namespace) {
      this.writeNamespace(namespace);
    }
  }

  private boolean shouldPermissionBeRemoved(RepositoryPermission permission, String permissionName) {
    return permission.getVerbs().contains(permissionName);
  }

  private RepositoryPermission removePermissionFromVerbs(RepositoryPermission permission, String permissionName) {
    return new RepositoryPermission(
      permission.getName(),
      permission
        .getVerbs()
        .stream()
        .filter(verb -> !verb.equals(permissionName))
        .collect(Collectors.toUnmodifiableSet()),
      permission.isGroupPermission()
    );
  }

  private void writeRepository(Repository repository, Path repositoryPath) {
    try {
      Marshaller marshaller = jaxbRepositoryContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(repository, repositoryPath.resolve("metadata.xml").toFile());
    } catch (JAXBException e) {
      throw new UpdateException("could not write repository structure", e);
    }
  }

  private void writeNamespace(Namespace namespace) {
    this.namespaceStore.put(namespace.getId(), namespace);
  }
}
