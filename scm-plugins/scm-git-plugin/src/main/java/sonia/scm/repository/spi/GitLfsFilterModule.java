package sonia.scm.repository.spi;

import com.google.common.io.ByteStreams;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.lib.Repository;
import sonia.scm.plugin.Extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

@Extension
public class GitLfsFilterModule implements Module {

  public static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("git-lfs (smudge|clean) -- .*");

  @Override
  public void configure(Binder binder) {
    FilterCommandRegistry.register(COMMAND_NAME_PATTERN, NoOpFilterCommand::new);
  }

  void unregister() {
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
