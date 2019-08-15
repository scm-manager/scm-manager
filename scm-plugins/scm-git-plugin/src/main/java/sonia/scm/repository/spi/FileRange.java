package sonia.scm.repository.spi;

public class FileRange {

  private final int start;
  private final int lineCount;

  public FileRange(int start, int lineCount) {
    this.start = start;
    this.lineCount = lineCount;
  }

  public int getStart() {
    return start;
  }

  public int getLineCount() {
    return lineCount;
  }
}
