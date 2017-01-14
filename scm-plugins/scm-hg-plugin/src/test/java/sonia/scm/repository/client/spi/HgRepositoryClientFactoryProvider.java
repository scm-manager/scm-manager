/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository.client.spi;

import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.PullCommand;
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
 * @author Sebastian Sdorra
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
