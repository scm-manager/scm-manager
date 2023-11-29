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
    
package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import jakarta.inject.Provider;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamespaceStrategyResourceTest {

  @Mock
  private UriInfo uriInfo;

  @Test
  void shouldReturnNamespaceStrategies() {
    when(uriInfo.getAbsolutePath()).thenReturn(URI.create("/namespace-strategies"));

    Set<NamespaceStrategy> namespaceStrategies = allStrategies();
    Provider<NamespaceStrategy> current = Providers.of(new MegaNamespaceStrategy());

    NamespaceStrategyResource resource = new NamespaceStrategyResource(namespaceStrategies, current);

    NamespaceStrategiesDto dto = resource.get(uriInfo);
    assertThat(dto.getCurrent()).isEqualTo(MegaNamespaceStrategy.class.getSimpleName());
    assertThat(dto.getAvailable()).contains(
      AwesomeNamespaceStrategy.class.getSimpleName(),
      SuperNamespaceStrategy.class.getSimpleName(),
      MegaNamespaceStrategy.class.getSimpleName()
    );
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/namespace-strategies");
  }

  private Set<NamespaceStrategy> allStrategies() {
    return  strategies(new AwesomeNamespaceStrategy(), new SuperNamespaceStrategy(), new MegaNamespaceStrategy());
  }

  private Set<NamespaceStrategy> strategies(NamespaceStrategy... strategies) {
    return new LinkedHashSet<>(Lists.newArrayList(strategies));
  }

  private static class AwesomeNamespaceStrategy implements NamespaceStrategy {
    @Override
    public String createNamespace(Repository repository) {
      return "awesome";
    }
  }

  private static class SuperNamespaceStrategy implements NamespaceStrategy {
    @Override
    public String createNamespace(Repository repository) {
      return "super";
    }
  }

  private static class MegaNamespaceStrategy implements NamespaceStrategy {
    @Override
    public String createNamespace(Repository repository) {
      return "mega";
    }
  }
}
