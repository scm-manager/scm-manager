package sonia.scm.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryTestData;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryStoreImporterTest {
  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryLocationResolver locationResolver;

  @InjectMocks
  private RepositoryStoreImporter repositoryStoreImporter;

  @Test
  void shouldImportStoresFromArchive(@TempDir Path temp) {
    when(locationResolver.supportsLocationType(Path.class)).thenReturn(true);
    when(locationResolver.forClass(Path.class).getLocation(REPOSITORY.getId())).thenReturn(temp);

    StoreEntryImporterFactory storeEntryImporterFactory = repositoryStoreImporter.doImport(REPOSITORY);
    assertThat(storeEntryImporterFactory).isNotNull();
  }
}
