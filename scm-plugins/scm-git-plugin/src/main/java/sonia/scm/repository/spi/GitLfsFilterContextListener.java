package sonia.scm.repository.spi;

import com.google.common.io.ByteStreams;
import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Extension
public class GitLfsFilterContextListener implements ServletContextListener {

  public static final String GITCONFIG = "[filter \"lfs\"]\n" +
        "clean = git-lfs clean -- %f\n" +
        "smudge = git-lfs smudge -- %f\n" +
        "process = git-lfs filter-process\n" +
        "required = true\n";
  public static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("git-lfs (smudge|clean) -- .*");

  private static final Logger LOG = LoggerFactory.getLogger(GitLfsFilterContextListener.class);

  private final SCMContextProvider contextProvider;

  @Inject
  public GitLfsFilterContextListener(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    Path gitconfig = contextProvider.getBaseDirectory().toPath().resolve("gitconfig");
    try {
      Files.write(gitconfig, GITCONFIG.getBytes(Charset.defaultCharset()), TRUNCATE_EXISTING, CREATE);
      FS.DETECTED.setGitSystemConfig(gitconfig.toFile());
      LOG.info("wrote git config file: {}", gitconfig);
    } catch (IOException e) {
      LOG.error("could not write git config in path {}; git lfs support may not work correctly", gitconfig, e);
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
