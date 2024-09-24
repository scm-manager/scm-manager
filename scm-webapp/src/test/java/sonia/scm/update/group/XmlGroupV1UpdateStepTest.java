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

package sonia.scm.update.group;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory;
import sonia.scm.update.UpdateStepTestUtil;
import sonia.scm.update.V1Properties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory.create;

@ExtendWith(MockitoExtension.class)
class XmlGroupV1UpdateStepTest {

  @Mock
  XmlGroupDAO groupDAO;

  @Captor
  ArgumentCaptor<Group> groupCaptor;

  InMemoryByteConfigurationEntryStoreFactory storeFactory = create();

  XmlGroupV1UpdateStep updateStep;

  private UpdateStepTestUtil testUtil;


  @BeforeEach
  void mockScmHome(@TempDir Path tempDir) {
    testUtil = new UpdateStepTestUtil(tempDir);
    updateStep = new XmlGroupV1UpdateStep(testUtil.getContextProvider(), groupDAO, storeFactory);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(groupDAO).add(groupCaptor.capture());
    }

    @BeforeEach
    void createGroupV1XML() throws IOException {
      testUtil.copyConfigFile("sonia/scm/update/group/groups.xml");
    }

    @Test
    void shouldCreateNewGroupFromGroupsV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(groupDAO, times(2)).add(any());
    }

    @Test
    void shouldMapAttributesFromGroupsV1Xml() throws JAXBException {
      updateStep.doUpdate();
      Optional<Group> group = groupCaptor.getAllValues().stream().filter(u -> u.getName().equals("normals")).findFirst();
      assertThat(group)
        .get()
        .hasFieldOrPropertyWithValue("name", "normals")
        .hasFieldOrPropertyWithValue("description", "Normal people")
        .hasFieldOrPropertyWithValue("type", "xml")
        .hasFieldOrPropertyWithValue("members", asList("trillian", "dent"))
        .hasFieldOrPropertyWithValue("lastModified", 1559550955883L)
        .hasFieldOrPropertyWithValue("creationDate", 1559548942457L);
    }

    @Test
    void shouldExtractProperties() throws JAXBException {
      updateStep.doUpdate();
      ConfigurationEntryStore<V1Properties> propertiesStore = storeFactory.get(V1Properties.class, "group-properties-v1");
      V1Properties properties = propertiesStore.get("normals");
      assertThat(properties).isNotNull();
      assertThat(properties.get("mostly")).isEqualTo("humans");
    }
  }

  @Nested
  class WithExistingDatabaseWithEmptyList {

    @BeforeEach
    void createGroupV1XML() throws IOException {
      testUtil.copyConfigFile("sonia/scm/update/group/groups_empty_groups.xml", "groups.xml");
    }

    @Test
    void shouldCreateNewGroupFromGroupsV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(groupDAO, times(0)).add(any());
    }
  }

  @Nested
  class WithExistingDatabaseWithoutList {

    @BeforeEach
    void createGroupV1XML() throws IOException {
      testUtil.copyConfigFile("sonia/scm/update/group/groups_no_groups.xml", "groups.xml");
    }

    @Test
    void shouldCreateNewGroupFromGroupsV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(groupDAO, times(0)).add(any());
    }
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
  }
}
