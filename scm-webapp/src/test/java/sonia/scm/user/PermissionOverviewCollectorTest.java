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

package sonia.scm.user;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.group.GroupCollector;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionOverviewCollectorTest {

  @Mock
  private GroupCollector groupCollector;
  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private GroupManager groupManager;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private NamespaceManager namespaceManager;

  @InjectMocks
  private PermissionOverviewCollector permissionOverviewCollector;

  private final String unknownGroupName = "hog";
  private final String knownGroupName = "earth";

  @BeforeEach
  void mockGroups() {
    when(groupCollector.fromLastLoginPlusInternal("trillian"))
      .thenReturn(Set.of(unknownGroupName, knownGroupName));
  }

  @BeforeEach
  void mockSubject() {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
  }

  @AfterEach
  void clearContext() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithGroups {
    @Test
    void shouldCollectGroupsFromGroupCollector() {
      when(groupManager.getAllNames()).thenReturn(singleton(knownGroupName));
      mockUnknownGroup(unknownGroupName);
      mockKnownGroup(knownGroupName);

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      Collection<PermissionOverview.GroupEntry> relevantGroups = permissionOverview.getRelevantGroups();
      assertThat(relevantGroups)
        .extracting("name")
        .contains(unknownGroupName, knownGroupName);
      assertThat(relevantGroups)
        .extracting("permissions")
        .contains(false, true);
      assertThat(relevantGroups)
        .extracting("externalOnly")
        .contains(false, true);
    }

    private void mockKnownGroup(String knownGroupName) {
      when(permissionAssigner.readPermissionsForGroup(knownGroupName))
        .thenReturn(singleton(new PermissionDescriptor()));
    }

    private void mockUnknownGroup(String unknownGroupName) {
      when(permissionAssigner.readPermissionsForGroup(unknownGroupName))
        .thenReturn(Collections.emptyList());
    }
  }

  @Nested
  class WithNamespaces {

    private Namespace namespace = new Namespace("git");

    @BeforeEach
    void mockNamespace() {
      when(namespaceManager.getAll())
        .thenReturn(singletonList(namespace));
    }

    @Test
    void shouldFindNamespaces() {
      namespace.addPermission(new RepositoryPermission("trillian", "read", false));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantNamespaces())
        .contains("git");
    }

    @Test
    void shouldFindNamespacesWithPermissionForUser() {
      namespace.addPermission(new RepositoryPermission("trillian", "read", false));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantNamespaces())
        .contains("git");
    }

    @Test
    void shouldFindNamespaceWithPermissionForGroupOfUser() {
      namespace.addPermission(new RepositoryPermission(knownGroupName, "read", true));
      when(groupCollector.fromLastLoginPlusInternal("trillian"))
        .thenReturn(singleton(knownGroupName));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantNamespaces())
        .contains("git");
    }

    @Test
    void shouldIgnoreNamespaceWithPermissionForOtherUser() {
      namespace.addPermission(new RepositoryPermission("arthur", "read", false));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantNamespaces())
        .doesNotContain("git");
    }

    @Test
    void shouldIgnoreRepositoryWithPermissionForOtherGroups() {
      namespace.addPermission(new RepositoryPermission("vogons", "read", true));
      when(groupCollector.fromLastLoginPlusInternal("trillian"))
        .thenReturn(singleton(knownGroupName));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantNamespaces())
        .doesNotContain("git");
    }
  }

  @Nested
  class WithRepositories {

    private final Repository repository = RepositoryTestData.create42Puzzle();

    @BeforeEach
    void mockRepository() {
      when(repositoryManager.getAll())
        .thenReturn(singletonList(repository));
    }

    @Test
    void shouldFindRepositoryWithPermissionForUser() {
      repository.addPermission(new RepositoryPermission("trillian", "read", false));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantRepositories())
        .contains(repository);
    }

    @Test
    void shouldFindRepositoryWithPermissionForGroupOfUser() {
      repository.addPermission(new RepositoryPermission(knownGroupName, "read", true));
      when(groupCollector.fromLastLoginPlusInternal("trillian"))
        .thenReturn(singleton(knownGroupName));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantRepositories())
        .contains(repository);
    }

    @Test
    void shouldIgnoreRepositoryWithPermissionForOtherUser() {
      repository.addPermission(new RepositoryPermission("arthur", "read", false));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantRepositories())
        .doesNotContain(repository);
    }

    @Test
    void shouldIgnoreRepositoryWithPermissionForOtherGroups() {
      repository.addPermission(new RepositoryPermission("vogons", "read", true));
      when(groupCollector.fromLastLoginPlusInternal("trillian"))
        .thenReturn(singleton(knownGroupName));

      PermissionOverview permissionOverview = permissionOverviewCollector.create("trillian");

      assertThat(permissionOverview.getRelevantRepositories())
        .doesNotContain(repository);
    }
  }
}
