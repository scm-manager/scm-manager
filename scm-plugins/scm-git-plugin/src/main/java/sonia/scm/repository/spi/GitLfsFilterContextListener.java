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

import com.google.common.io.ByteStreams;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

@Extension
public class GitLfsFilterContextListener implements ServletContextListener {

  public static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("git-lfs (smudge|clean) -- .*");

  private static final Logger LOG = LoggerFactory.getLogger(GitLfsFilterContextListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "clean", "git-lfs clean -- %f");
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "smudge", "git-lfs smudge -- %f");
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "process", "git-lfs filter-process");
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "required", "true");
    } catch (Exception e) {
      LOG.error("could not set git config; git lfs support may not work correctly", e);
    }
    FilterCommandRegistry.register(COMMAND_NAME_PATTERN, NoOpFilterCommand::new);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    FilterCommandRegistry.unregister(COMMAND_NAME_PATTERN);
  }

  private static class NoOpFilterCommand extends FilterCommand {
    NoOpFilterCommand(Repository db, InputStream in, OutputStream out) {
      super(in, out);
    }

    @Override
    public int run() throws IOException {
      ByteStreams.copy(in, out);
      in.close();
      out.close();
      return -1;
    }
  }
}
