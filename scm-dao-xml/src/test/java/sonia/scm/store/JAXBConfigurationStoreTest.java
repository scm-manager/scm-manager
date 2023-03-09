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

package sonia.scm.store;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryReadOnlyChecker;

import java.util.Collections;

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
 * @author Sebastian Sdorra
 */
public class JAXBConfigurationStoreTest extends StoreTestBase {

  private final RepositoryReadOnlyChecker readOnlyChecker = mock(RepositoryReadOnlyChecker.class);

  @Override
  protected JAXBConfigurationStoreFactory createStoreFactory() {
    return new JAXBConfigurationStoreFactory(contextProvider, repositoryLocationResolver, readOnlyChecker, emptySet());
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
