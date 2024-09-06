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

package sonia.scm.store;

import org.junit.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryReadOnlyChecker;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JAXBConfigurationStore}.
 *
 */
public class JAXBConfigurationStoreTest extends StoreTestBase {

  private final RepositoryReadOnlyChecker readOnlyChecker = mock(RepositoryReadOnlyChecker.class);

  @Override
  protected JAXBConfigurationStoreFactory createStoreFactory() {
    return new JAXBConfigurationStoreFactory(contextProvider, repositoryLocationResolver, readOnlyChecker, emptySet(), new StoreCacheConfigProvider(false));
  }


  @Test
  public void shouldStoreAndLoadInRepository() {
    Repository repository = new Repository("id", "git", "ns", "n");
    ConfigurationStore<StoreObject> store = createStoreFactory()
      .withType(StoreObject.class)
      .withName("test")
      .forRepository(repository)
      .build();

    store.set(new StoreObject("value"));
    StoreObject storeObject = store.get();

    assertNotNull(storeObject);
    assertEquals("value", storeObject.getValue());
  }


  @Test
  public void shouldNotWriteArchivedRepository() {
    Repository repository = new Repository("id", "git", "ns", "n");
    when(readOnlyChecker.isReadOnly("id")).thenReturn(true);
    ConfigurationStore<StoreObject> store = createStoreFactory()
      .withType(StoreObject.class)
      .withName("test")
      .forRepository(repository)
      .build();

    StoreObject storeObject = new StoreObject("value");
    assertThrows(RuntimeException.class, () -> store.set(storeObject));
  }

  @Test
  public void shouldDeleteConfigStore() {
    Repository repository = new Repository("id", "git", "ns", "n");
    ConfigurationStore<StoreObject> store = createStoreFactory()
      .withType(StoreObject.class)
      .withName("test")
      .forRepository(repository)
      .build();

    store.set(new StoreObject("value"));

    store.delete();
    StoreObject storeObject = store.get();

    assertThat(storeObject).isNull();
  }

  @Test
  public void shouldNotDeleteStoreForArchivedRepository() {
    Repository repository = new Repository("id", "git", "ns", "n");
    when(readOnlyChecker.isReadOnly("id")).thenReturn(false);
    ConfigurationStore<StoreObject> store = createStoreFactory()
      .withType(StoreObject.class)
      .withName("test")
      .forRepository(repository)
      .build();

    store.set(new StoreObject());
    when(readOnlyChecker.isReadOnly("id")).thenReturn(true);

    assertThrows(StoreReadOnlyException.class, store::delete);
    assertThat(store.getOptional()).isPresent();
  }
}
