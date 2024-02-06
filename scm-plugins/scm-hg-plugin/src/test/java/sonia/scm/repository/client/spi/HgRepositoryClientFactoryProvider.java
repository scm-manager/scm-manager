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
    
package sonia.scm.repository.client.spi;

import org.javahg.Repository;
import org.javahg.RepositoryConfiguration;
import org.javahg.commands.ExecutionException;
import org.javahg.commands.PullCommand;
import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.repository.client.api.RepositoryClientException;
import sonia.scm.util.IOUtil;

/**
 * Mercurial implementation of the {@link RepositoryClientFactoryProvider}.
 *
 */
public class HgRepositoryClientFactoryProvider implements RepositoryClientFactoryProvider
{
  
  private static final String TYPE = "hg";
  
  @Override
  public RepositoryClientProvider create(File main, File workingCopy) throws IOException {
    return create(main.toURI().toString(), null, null, workingCopy);
  }
  
  @Override
  public RepositoryClientProvider create(String url, String username, String password, File workingCopy) 
    throws IOException {
    RepositoryConfiguration configuration = new RepositoryConfiguration();
    String binary = IOUtil.search("hg");
    if (Strings.isNullOrEmpty(binary)){
      throw new RepositoryClientException("could not find mercurial binary (hg)");
    }
    configuration.setHgBin(binary);
    
    File hgrc = null;
    if (!Strings.isNullOrEmpty(username))
    {
      hgrc = createHgrc(url, username, password);
      configuration.setHgrcPath(hgrc.getAbsolutePath());
    }
    
    Repository repository = Repository.create(configuration, workingCopy);
    try {
      PullCommand command = PullCommand.on(repository);
      command.cmdAppend("-u");
      command.execute(url);
    } catch (ExecutionException ex) {
      throw new RepositoryClientException("failed to pull from remote repository", ex);
    }
    
    return new HgRepositoryClientProvider(repository, hgrc, url);
  }
  
  private File createHgrc(String url, String username, String password) throws IOException {
    URL repositoryUrl = new URL(url);
      
    INIConfiguration hgConfig = new INIConfiguration();
    
    INISection pathSection = new INISection("paths");
    pathSection.setParameter(repositoryUrl.getHost(), url);
    hgConfig.addSection(pathSection);

    String prefix = repositoryUrl.getHost() + ".";

    INISection authSection = new INISection("auth");
    authSection.setParameter(
      prefix + "prefix", 
      repositoryUrl.getHost() + ":" + repositoryUrl.getPort() + repositoryUrl.getPath()
    );
    authSection.setParameter(prefix + "schemes", repositoryUrl.getProtocol());
    authSection.setParameter(prefix + "username", username);
    if (!Strings.isNullOrEmpty(password)) {
      authSection.setParameter(prefix + "password", password);
    }
    hgConfig.addSection(authSection);

    File hgrc = File.createTempFile("hgrc", ".temp");
    INIConfigurationWriter writer = new INIConfigurationWriter();
    writer.write(hgConfig, hgrc);
    return hgrc;
  }

  @Override
  public String getType() {
    return TYPE;
  }
  
}
