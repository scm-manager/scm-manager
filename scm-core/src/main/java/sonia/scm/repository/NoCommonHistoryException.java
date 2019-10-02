package sonia.scm.repository;

import sonia.scm.BadRequestException;

import static java.util.Collections.emptyList;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NoCommonHistoryException extends BadRequestException {

  public NoCommonHistoryException() {
    this("no common history");
  }

  public NoCommonHistoryException(String message) {
    super(emptyList(), message);
  }

  @Override
  public String getCode() {
    return "4iRct4avG1";
  }
}
