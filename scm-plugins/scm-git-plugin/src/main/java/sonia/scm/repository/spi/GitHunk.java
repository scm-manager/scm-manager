package sonia.scm.repository.spi;

import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.Hunk;

import java.util.Iterator;
import java.util.List;

public class GitHunk implements Hunk {

  private final FileRange oldFileRange;
  private final FileRange newFileRange;
  private List<DiffLine> lines;

  public GitHunk(FileRange oldFileRange, FileRange newFileRange) {
    this.oldFileRange = oldFileRange;
    this.newFileRange = newFileRange;
  }

  @Override
  public int getOldStart() {
    return oldFileRange.getStart();
  }

  @Override
  public int getOldLineCount() {
    return oldFileRange.getLineCount();
  }

  @Override
  public int getNewStart() {
    return newFileRange.getStart();
  }

  @Override
  public int getNewLineCount() {
    return newFileRange.getLineCount();
  }

  @Override
  public Iterator<DiffLine> iterator() {
    return lines.iterator();
  }

  void setLines(List<DiffLine> lines) {
    this.lines = lines;
  }
}
