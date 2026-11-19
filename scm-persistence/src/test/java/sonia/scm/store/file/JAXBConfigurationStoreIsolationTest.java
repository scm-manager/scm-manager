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

package sonia.scm.store.file;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sonia.scm.AbstractTestBase;
import sonia.scm.auditlog.AuditLogConfigurationStoreDecoratorFactory;
import sonia.scm.auditlog.Auditor;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.store.ConfigurationStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JAXBConfigurationStoreIsolationTest extends AbstractTestBase {

  private final Repository repository = new Repository("42", "git", "space", "mirror");

  @Test
  void shouldIsolateLoadedConfiguration() {
    createStore(Set.of()).set(configuration());
    ConfigurationStore<Configuration> store = createStore(Set.of());

    Configuration loaded = store.get();
    loaded.credentials.username = "changed";
    loaded.patterns.add("feature/*");

    assertOriginalConfiguration(store.get());
    assertOriginalConfiguration(createStore(Set.of()).get());
  }

  @Test
  void shouldIsolateWrittenConfiguration() {
    ConfigurationStore<Configuration> store = createStore(Set.of());
    Configuration configuration = configuration();

    store.set(configuration);
    configuration.credentials.username = "changed";
    configuration.patterns.add("feature/*");

    assertOriginalConfiguration(store.get());
    assertOriginalConfiguration(createStore(Set.of()).get());
  }

  @Test
  void shouldAuditPersistedBeforeValueWhenTheInitialValueIsModifiedAndSetAgain() {
    shouldAuditPersistedBeforeValue(false);
  }

  @Test
  void shouldAuditPersistedBeforeValueWhenTheStoredObjectIsModifiedAndSetAgain() {
    shouldAuditPersistedBeforeValue(true);
  }

  private void shouldAuditPersistedBeforeValue(boolean mutateSetArgument) {
    Auditor auditor = mock(Auditor.class);
    ConfigurationStore<Configuration> store = createStore(Set.of(auditor));
    Configuration initial = configuration();
    store.set(initial);
    clearInvocations(auditor);

    Configuration modified = mutateSetArgument ? initial : store.get();
    modified.credentials.username = "changed";
    modified.patterns.add("feature/*");
    store.set(modified);

    verify(auditor).createEntry(argThat(context -> {
      assertOriginalConfiguration((Configuration) context.getOldObject());
      Configuration newConfiguration = (Configuration) context.getObject();
      assertThat(newConfiguration.credentials.username).isEqualTo("changed");
      assertThat(newConfiguration.patterns).containsExactly("main", "feature/*");
      return true;
    }));
  }

  private ConfigurationStore<Configuration> createStore(Set<Auditor> auditors) {
    RepositoryDAO repositoryDAO = mock(RepositoryDAO.class);
    when(repositoryDAO.get(repository.getId())).thenReturn(repository);
    JAXBConfigurationStoreFactory factory = new JAXBConfigurationStoreFactory(
      contextProvider,
      repositoryLocationResolver,
      mock(RepositoryReadOnlyChecker.class),
      Set.of(new AuditLogConfigurationStoreDecoratorFactory(auditors, repositoryDAO)),
      new StoreCacheFactory(new StoreCacheConfigProvider(false))
    );
    return factory.withType(Configuration.class).withName("isolation").forRepository(repository).build();
  }

  private Configuration configuration() {
    Configuration configuration = new Configuration();
    configuration.credentials.username = "original";
    configuration.patterns.add("main");
    return configuration;
  }

  private void assertOriginalConfiguration(Configuration configuration) {
    assertThat(configuration.credentials.username).isEqualTo("original");
    assertThat(configuration.patterns).containsExactly("main");
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Configuration {
    private Credentials credentials = new Credentials();
    private List<String> patterns = new ArrayList<>();
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Credentials {
    private String username;
  }
}
