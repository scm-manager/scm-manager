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
