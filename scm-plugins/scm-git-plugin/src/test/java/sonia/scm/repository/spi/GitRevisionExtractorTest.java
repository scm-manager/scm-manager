package sonia.scm.repository.spi;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitRevisionExtractorTest {

  @Test
  void shouldReturnRevisionFromRevCommit() {
    RevCommit revCommit = mock(RevCommit.class);
    when(revCommit.toString()).thenReturn("commit 123456abcdef -t 4561");
    String revision = GitRevisionExtractor.extractRevisionFromRevCommit(revCommit);
    assertThat(revision).isEqualTo("123456abcdef");
  }
}
