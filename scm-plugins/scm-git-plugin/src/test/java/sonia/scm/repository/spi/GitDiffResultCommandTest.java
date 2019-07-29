package sonia.scm.repository.spi;

import org.junit.Test;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffResult;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class GitDiffResultCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldReturnOldAndNewRevision() throws IOException {
    GitDiffResultCommand gitDiffResultCommand = new GitDiffResultCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    DiffResult diffResult = gitDiffResultCommand.getDiffResult(diffCommandRequest);

    assertThat(diffResult.getNewRevision()).isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertThat(diffResult.getOldRevision()).isEqualTo("592d797cd36432e591416e8b2b98154f4f163411");
  }

  @Test
  public void shouldReturnFilePaths() throws IOException {
    GitDiffResultCommand gitDiffResultCommand = new GitDiffResultCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    DiffResult diffResult = gitDiffResultCommand.getDiffResult(diffCommandRequest);
    Iterator<DiffFile> iterator = diffResult.iterator();
    DiffFile a = iterator.next();
    assertThat(a.getNewPath()).isEqualTo("a.txt");
    assertThat(a.getOldPath()).isEqualTo("a.txt");

    DiffFile b = iterator.next();
    assertThat(b.getOldPath()).isEqualTo("b.txt");
    assertThat(b.getNewPath()).isEqualTo("/dev/null");
  }
}
