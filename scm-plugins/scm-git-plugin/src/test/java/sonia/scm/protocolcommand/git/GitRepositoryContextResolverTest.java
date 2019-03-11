package sonia.scm.protocolcommand.git;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitRepositoryContextResolverTest {

  private static final Repository REPOSITORY = new Repository("id", "git", "space", "X");

  @Mock
  RepositoryManager repositoryManager;
  @Mock
  RepositoryLocationResolver locationResolver;

  @InjectMocks
  GitRepositoryContextResolver resolver;

  @Test
  void shouldResolveCorrectRepository() throws IOException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(REPOSITORY);
    Path repositoryPath = File.createTempFile("test", "scm").toPath();
    when(locationResolver.getPath("id")).thenReturn(repositoryPath);

    RepositoryContext context = resolver.resolve(new String[] {"git", "repo/space/X/something/else"});

    assertThat(context.getRepository()).isSameAs(REPOSITORY);
    assertThat(context.getDirectory()).isEqualTo(repositoryPath.resolve("data"));
  }
}
