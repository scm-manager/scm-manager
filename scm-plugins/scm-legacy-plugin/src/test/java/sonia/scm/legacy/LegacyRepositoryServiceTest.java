package sonia.scm.legacy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyRepositoryServiceTest {

  @Mock
  private RepositoryManager repositoryManager;

  private LegacyRepositoryService legacyRepositoryService;
  private final Repository repository = new Repository("abc123", "git", "space", "repo");

  @Before
  public void init() {
    legacyRepositoryService  = new LegacyRepositoryService(repositoryManager);
  }

  @Test
  public void findRepositoryNameSpaceAndNameForRepositoryId() {
    when(repositoryManager.get(any(String.class))).thenReturn(repository);
    NamespaceAndNameDto namespaceAndName = legacyRepositoryService.getNameAndNamespaceForRepositoryId("abc123");
    assertThat(namespaceAndName.getName()).isEqualToIgnoringCase("repo");
    assertThat(namespaceAndName.getNamespace()).isEqualToIgnoringCase("space");
  }

  @Test(expected = NotFoundException.class)
  public void shouldGetNotFoundExceptionIfRepositoryNotExists() throws NotFoundException {
    when(repositoryManager.get(any(String.class))).thenReturn(null);
    legacyRepositoryService.getNameAndNamespaceForRepositoryId("456def");
  }
}
