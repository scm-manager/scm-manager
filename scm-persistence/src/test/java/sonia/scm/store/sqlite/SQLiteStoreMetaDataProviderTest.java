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

package sonia.scm.store.sqlite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.ExtensionProcessor;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.store.QueryableType;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQLiteStoreMetaDataProviderTest {

  @Mock
  private PluginLoader pluginLoader;
  @Mock
  private ExtensionProcessor extensionProcessor;
  @Mock
  private QueryableTypeDescriptor descriptor1;
  @Mock
  private QueryableTypeDescriptor descriptor2;

  private SQLiteStoreMetaDataProvider metaDataProvider;

  @BeforeEach
  void setUp() {
    when(descriptor1.getTypes()).thenReturn(new String[]{"sonia.scm.store.sqlite.TestParent1.class"});
    when(descriptor1.getClazz()).thenReturn("sonia.scm.store.sqlite.TestChildWithOneParent");

    when(descriptor2.getTypes()).thenReturn(new String[]{"sonia.scm.store.sqlite.TestParent1.class", "sonia.scm.store.sqlite.TestParent2.class"});
    when(descriptor2.getClazz()).thenReturn("sonia.scm.store.sqlite.TestChildWithTwoParent");

    when(extensionProcessor.getQueryableTypes()).thenReturn(List.of(descriptor1, descriptor2));

    when(pluginLoader.getUberClassLoader()).thenReturn(this.getClass().getClassLoader());
    when(pluginLoader.getExtensionProcessor()).thenReturn(extensionProcessor);

    metaDataProvider = new SQLiteStoreMetaDataProvider(pluginLoader);
  }

  @Test
  void testInitializeType() {
    Collection<Class<?>> parent1Types = metaDataProvider.getTypesWithParent(TestParent1.class);
    assertThat(parent1Types)
      .extracting("name")
      .containsExactly(
        "sonia.scm.store.sqlite.TestChildWithOneParent",
        "sonia.scm.store.sqlite.TestChildWithTwoParent"
      );

    Collection<Class<?>> parent2Types = metaDataProvider.getTypesWithParent(TestParent1.class, TestParent2.class);
    assertThat(parent2Types)
      .extracting("name")
      .containsExactly("sonia.scm.store.sqlite.TestChildWithTwoParent");
  }
}

class TestParent1 {
}

class TestParent2 {
}

@QueryableType(TestParent1.class)
class TestChildWithOneParent {
}

@QueryableType({TestParent1.class, TestParent2.class})
class TestChildWithTwoParent {
}
