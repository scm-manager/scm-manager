package sonia.scm.repository.spi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GitDiffCommand_DequoteOutputStreamTest {

  @Test
  void shouldDequoteText() throws IOException {
    String s = "diff --git \"a/file \\303\\272\\303\\274\\303\\276\\303\\253\\303\\251\\303\\245\\303\\253\\303\\245\\303\\251 a\" \"b/file \\303\\272\\303\\274\\303\\276\\303\\253\\303\\251\\303\\245\\303\\253\\303\\245\\303\\251 b\"\n" +
      "new file mode 100644\n" +
      "index 0000000..8cb0607\n" +
      "--- /dev/null\n" +
      "+++ \"b/\\303\\272\\303\\274\\303\\276\\303\\253\\303\\251\\303\\245\\303\\253\\303\\245\\303\\251 \\303\\245g\\303\\260f\\303\\237\"\n" +
      "@@ -0,0 +1 @@\n" +
      "+String s = \"quotes shall be kept\";";

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    GitDiffCommand.DequoteOutputStream stream = new GitDiffCommand.DequoteOutputStream(buffer);
    byte[] bytes = s.getBytes();
    stream.write(bytes, 0, bytes.length);
    stream.flush();

    Assertions.assertThat(buffer.toString()).isEqualTo("diff --git a/file úüþëéåëåé a b/file úüþëéåëåé b\n" +
      "new file mode 100644\n" +
      "index 0000000..8cb0607\n" +
      "--- /dev/null\n" +
      "+++ b/úüþëéåëåé ågðfß\n" +
      "@@ -0,0 +1 @@\n" +
      "+String s = \"quotes shall be kept\";");
  }
}
