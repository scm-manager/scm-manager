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
