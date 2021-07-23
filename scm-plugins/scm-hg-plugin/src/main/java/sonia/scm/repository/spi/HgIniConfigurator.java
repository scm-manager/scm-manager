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

package sonia.scm.repository.spi;

import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.repository.HgRepositoryHandler;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class HgIniConfigurator {

  private final HgCommandContext context;
  private static final String AUTH_SECTION = "auth";

  public HgIniConfigurator(HgCommandContext context) {
    this.context = context;
  }

  public void addAuthenticationConfig(RemoteCommandRequest request, String url) throws IOException {
    INIConfiguration ini = readIniConfiguration();
    INISection authSection = ini.getSection(AUTH_SECTION);
    if (authSection == null) {
      authSection = new INISection(AUTH_SECTION);
      ini.addSection(authSection);
    }
    URI parsedUrl = URI.create(url);
    authSection.setParameter("import.prefix", parsedUrl.getHost());
    authSection.setParameter("import.schemes", parsedUrl.getScheme());
    authSection.setParameter("import.username", request.getUsername());
    authSection.setParameter("import.password", request.getPassword());
    writeIniConfiguration(ini);
  }

  public void removeAuthenticationConfig() throws IOException {
    INIConfiguration ini = readIniConfiguration();
    ini.removeSection(AUTH_SECTION);
    writeIniConfiguration(ini);
  }

  public INIConfiguration readIniConfiguration() throws IOException {
    return new INIConfigurationReader().read(getHgrcFile());
  }

  public void writeIniConfiguration(INIConfiguration ini) throws IOException {
    new INIConfigurationWriter().write(ini, getHgrcFile());
  }

  public File getHgrcFile() {
    return new File(context.getDirectory(), HgRepositoryHandler.PATH_HGRC);
  }

}
