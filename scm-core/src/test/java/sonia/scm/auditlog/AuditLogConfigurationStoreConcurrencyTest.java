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

package sonia.scm.auditlog;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sonia.scm.store.AbstractStore;
import sonia.scm.store.ConcurrentStoreOperations;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.StoreDecoratorFactory;
import sonia.scm.store.TypedStoreParameters;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditLogConfigurationStoreConcurrencyTest {

  @ParameterizedTest
  @CsvSource({"false, false", "false, true", "true, false"})
  @SuppressWarnings("unchecked")
  void shouldAuditConsecutiveChanges(boolean deleteFirst, boolean deleteSecond) throws Exception {
    ConcurrentStoreOperations operations = new ConcurrentStoreOperations();
    List<EntryCreationContext<?>> entries = new CopyOnWriteArrayList<>();
    Auditor auditor = context -> {
      operations.pauseFirstOperation();
      entries.add(context);
    };
    TypedStoreParameters<String> parameters = mock(TypedStoreParameters.class);
    when(parameters.getName()).thenReturn("concurrency");
    TestStore delegate = new TestStore();
    delegate.set("original");
    ConfigurationStore<String> store = new AuditLogConfigurationStoreDecorator<>(
      Set.of(auditor), null, delegate, new StoreDecoratorFactory.Context(parameters)
    );

    operations.run(() -> change(store, deleteFirst, "first"), () -> change(store, deleteSecond, "second"));

    String firstValue = deleteFirst ? null : "first";
    String secondValue = deleteSecond ? null : "second";
    assertThat(entries)
      .extracting(EntryCreationContext::getOldObject, EntryCreationContext::getObject)
      .containsExactly(tuple("original", firstValue), tuple(firstValue, secondValue));
    assertThat(store.get()).isEqualTo(secondValue);
    assertThat(delegate.persisted).isEqualTo(secondValue);
  }

  private void change(ConfigurationStore<String> store, boolean delete, String value) {
    if (delete) {
      store.delete();
    } else {
      store.set(value);
    }
  }

  private static class TestStore extends AbstractStore<String> {

    private String persisted;

    private TestStore() {
      super(() -> false);
    }

    @Override
    protected String readObject() {
      return persisted;
    }

    @Override
    protected String copy(String object) {
      return object;
    }

    @Override
    protected void writeObject(String object) {
      persisted = object;
    }

    @Override
    protected void deleteObject() {
      persisted = null;
    }
  }
}
