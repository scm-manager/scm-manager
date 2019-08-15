package sonia.scm.repository.api;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

class HunkTest {

  @Test
  void shouldGetComplexHeader() {
    String rawHeader = createHunk(2, 3, 4, 5).getRawHeader();

    assertThat(rawHeader).isEqualTo("@@ -2,3 +4,5 @@");
  }

  @Test
  void shouldReturnSingleNumberForOne() {
    String rawHeader = createHunk(42, 1, 5, 1).getRawHeader();

    assertThat(rawHeader).isEqualTo("@@ -42 +5 @@");
  }

  private Hunk createHunk(int oldStart, int oldLineCount, int newStart, int newLineCount) {
    return new Hunk() {
      @Override
      public int getOldStart() {
        return oldStart;
      }

      @Override
      public int getOldLineCount() {
        return oldLineCount;
      }

      @Override
      public int getNewStart() {
        return newStart;
      }

      @Override
      public int getNewLineCount() {
        return newLineCount;
      }

      @Override
      public Iterator<DiffLine> iterator() {
        return null;
      }
    };
  }
}
