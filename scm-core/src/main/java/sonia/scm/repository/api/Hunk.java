package sonia.scm.repository.api;

public interface Hunk extends Iterable<DiffLine> {

  default String getRawHeader() {
    return String.format("@@ -%s +%s @@", getLineMarker(getOldStart(), getOldLineCount()), getLineMarker(getNewStart(), getNewLineCount()));
  }

  default String getLineMarker(int start, int lineCount) {
    if (lineCount == 1) {
      return Integer.toString(start);
    } else {
      return String.format("%s,%s", start, lineCount);
    }
  }

  int getOldStart();

  int getOldLineCount();

  int getNewStart();

  int getNewLineCount();
}
