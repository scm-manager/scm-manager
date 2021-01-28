package sonia.scm.importexport;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryMetadataXmlGeneratorTest {

  private final static Repository REPOSITORY = RepositoryTestData.createHeartOfGold("git");
  private RepositoryMetadataXmlGenerator generator = new RepositoryMetadataXmlGenerator();

  @Test
  void shouldCreateMetadataWithRepositoryType() {
    byte[] metadata = generator.generate(REPOSITORY);

    assertThat(new String(metadata)).contains("<type>git</type>");
  }

  @Test
  void shouldCreateMetadataWithRepositoryNamespaceAndName() {
    byte[] metadata = generator.generate(REPOSITORY);

    assertThat(new String(metadata)).contains("<namespace>hitchhiker</namespace>");
    assertThat(new String(metadata)).contains("<name>HeartOfGold</name>");
  }

  @Test
  void shouldCreateMetadataWithRepositoryContactAndDescription() {
    byte[] metadata = generator.generate(REPOSITORY);

    assertThat(new String(metadata)).contains("<contact>zaphod.beeblebrox@hitchhiker.com</contact>");
    assertThat(new String(metadata)).contains("<description>Heart of Gold is the first prototype ship to successfully utilise the revolutionary Infinite Improbability Drive</description>");
  }

  @Test
  void shouldCreateMetadataWithRepositoryPermissions() {
    REPOSITORY.addPermission(new RepositoryPermission("arthur", "READ", false));

    byte[] metadata = generator.generate(REPOSITORY);

    assertThat(new String(metadata)).contains("<permissions>");
    assertThat(new String(metadata)).contains("<name>arthur</name>");
    assertThat(new String(metadata)).contains("<role>READ</role>");
  }
}
