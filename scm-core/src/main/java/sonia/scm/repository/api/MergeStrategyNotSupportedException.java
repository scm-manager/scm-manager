package sonia.scm.repository.api;

import sonia.scm.BadRequestException;
import sonia.scm.repository.Repository;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class MergeStrategyNotSupportedException extends BadRequestException {

  private static final long serialVersionUID = 256498734456613496L;

  private static final String CODE = "6eRhF9gU41";

  public MergeStrategyNotSupportedException(Repository repository, MergeStrategy strategy) {
    super(entity(repository).build(), createMessage(strategy));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(MergeStrategy strategy) {
    return "merge strategy " + strategy + " is not supported by this repository";
  }
}
