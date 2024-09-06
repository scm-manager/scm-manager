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
