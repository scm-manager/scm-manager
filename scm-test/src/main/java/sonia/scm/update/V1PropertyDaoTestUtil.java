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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class V1PropertyDaoTestUtil {

  private final V1PropertyDAO propertyDAO = mock(V1PropertyDAO.class);

  public V1PropertyDAO getPropertyDAO() {
    return propertyDAO;
  }

  public void mockRepositoryProperties(PropertiesForRepository... mockedPropertiesForRepositories) {
    Map<String, V1Properties> map = new HashMap<>();
    stream(mockedPropertiesForRepositories).forEach(p -> map.put(p.repositoryId, p.asProperties()));
    V1PropertyReader.Instance v1PropertyReader = new MapBasedPropertyReaderInstance(map);
    when(propertyDAO.getProperties(argThat(argument -> argument instanceof RepositoryV1PropertyReader))).thenReturn(v1PropertyReader);
  }

  public static class PropertiesForRepository {
    private final String repositoryId;
    private final Map<String, String> properties;

    public PropertiesForRepository(String repositoryId, Map<String, String> properties) {
      this.repositoryId = repositoryId;
      this.properties = properties;
    }

    V1Properties asProperties() {
      return new V1Properties(properties.entrySet().stream().map(e -> new V1Property(e.getKey(), e.getValue())).collect(Collectors.toList()));
    }
  }
}
