package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadmeRepositoryContentInitializerTest {

  @Mock
  private RepositoryContentInitializer.InitializerContext context;

  @Mock
  private RepositoryContentInitializer.CreateFile createFile;

  private Repository repository;

  private ReadmeRepositoryContentInitializer initializer = new ReadmeRepositoryContentInitializer();

  @BeforeEach
  void setUpContext() {
    repository = RepositoryTestData.createHeartOfGold("hg");
    when(context.getRepository()).thenReturn(repository);
    when(context.create("README.md")).thenReturn(createFile);
  }

  @Test
  void shouldCreateReadme() throws IOException {
    initializer.initialize(context);

    verify(createFile).from("# HeartOfGold\n\n" + repository.getDescription());
  }

  @Test
  void shouldCreateReadmeWithoutDescription() throws IOException {
    repository.setDescription(null);

    initializer.initialize(context);

    verify(createFile).from("# HeartOfGold");
  }

}
