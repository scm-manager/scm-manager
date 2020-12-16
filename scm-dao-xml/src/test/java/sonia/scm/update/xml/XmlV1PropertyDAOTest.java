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

package sonia.scm.update.xml;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.repository.RepositoryArchivedCheck;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.update.RepositoryV1PropertyReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;


class XmlV1PropertyDAOTest {

  public static final String PROPERTIES = "<?xml version=\"1.0\" ?>\n" +
    "<configuration>\n" +
    "  <entry>\n" +
    "    <key>9ZQKlvI401</key>\n" +
    "    <value>\n" +
    "      <item></item>\n" +
    "      <item>\n" +
    "        <key>redmine.url</key>\n" +
    "        <value>https://redmine.example.net/projects/r6</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>redmine.auto-close-username-transformer</key>\n" +
    "        <value>{0}</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>redmine.auto-close</key>\n" +
    "      </item>\n" +
    "    </value>\n" +
    "  </entry>\n" +
    "  <entry>\n" +
    "    <key>1wPsrHPM81</key>\n" +
    "    <value>\n" +
    "      <item>\n" +
    "        <key>notify.contact.repository</key>\n" +
    "        <value>true</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>redmine.auto-close-username-transformer</key>\n" +
    "        <value>fixed, fix, closed, close, resolved, resolve</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>redmine.url</key>\n" +
    "        <value>https://redmine.example.net/projects/a2</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>redmine.update-issues</key>\n" +
    "        <value>true</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>notify.email.per.push</key>\n" +
    "        <value>true</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>notify.use.author.as.from.address</key>\n" +
    "        <value>true</value>\n" +
    "      </item>\n" +
    "      <item>\n" +
    "        <key>redmine.auto-close</key>\n" +
    "        <value>true</value>\n" +
    "      </item>\n" +
    "    </value>\n" +
    "  </entry>\n" +
    "  <entry>\n" +
    "    <key>WlDszQtZj4</key>\n" +
    "    <value></value>\n" +
    "  </entry>\n" +
    "</configuration>";

  /**
   * This is a test for https://github.com/scm-manager/scm-manager/issues/1237
   */
  @Test
  void shouldReadItemsWithEmptyValues(@TempDir Path temp) throws IOException {
    Path configPath = temp.resolve("config");
    Files.createDirectories(configPath);
    Path propFile = configPath.resolve("repository-properties-v1.xml");
    Files.write(propFile, PROPERTIES.getBytes());
    RepositoryArchivedCheck archivedCheck = mock(RepositoryArchivedCheck.class);
    XmlV1PropertyDAO dao = new XmlV1PropertyDAO(new JAXBConfigurationEntryStoreFactory(new SimpleContextProvider(temp), null, new SimpleKeyGenerator(), archivedCheck));

    dao.getProperties(new RepositoryV1PropertyReader())
      .forEachEntry((key, prop) -> {
        if (key.equals("9ZQKlvI401")) {
          Assertions.assertThat(prop.getBoolean("redmine.auto-close")).isNotPresent();
        } else if (key.equals("1wPsrHPM81")) {
          Assertions.assertThat(prop.getBoolean("redmine.auto-close")).isPresent().get().isEqualTo(true);
        }
      });
  }

  private static class SimpleContextProvider implements SCMContextProvider {
    private final Path temp;

    public SimpleContextProvider(Path temp) {
      this.temp = temp;
    }

    @Override
    public File getBaseDirectory() {
      return temp.toFile();
    }

    @Override
    public Path resolve(Path path) {
      return null;
    }

    @Override
    public Stage getStage() {
      return null;
    }

    @Override
    public Throwable getStartupError() {
      return null;
    }

    @Override
    public String getVersion() {
      return null;
    }
  }

  private static class SimpleKeyGenerator implements KeyGenerator {
    @Override
    public String createKey() {
      return "" + System.nanoTime();
    }
  }
}
