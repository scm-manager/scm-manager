/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
