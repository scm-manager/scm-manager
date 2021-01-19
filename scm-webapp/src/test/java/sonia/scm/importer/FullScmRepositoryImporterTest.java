package sonia.scm.importer;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FullScmRepositoryImporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryLocationResolver locationResolver;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ScmEnvironmentCompatibilityChecker compatibilityChecker;
  @Mock
  private RepositoryStoreImporter storeImporterFactory;

  @InjectMocks
  private FullScmRepositoryImporter fullImporter;

  @Test
  void shouldNotImportRepositoryIfFileNotExists() {
    assertThrows(ImportFailedException.class, () -> fullImporter.importFromFile(REPOSITORY, new FileInputStream(new File("not_existing"))));
  }

  @Test
  void shouldFailIfScmEnvironmentIsIncompatible() throws IOException {
    fullImporter.importFromFile(REPOSITORY, Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream());
  }

}
