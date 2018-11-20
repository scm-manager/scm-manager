package sonia.scm;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

public class ScmConstraintViolationException extends RuntimeException {

  private final Collection<ScmConstraintViolation> violations;

  private final String furtherInformations;

  private ScmConstraintViolationException(Collection<ScmConstraintViolation> violations, String furtherInformations) {
    this.violations = violations;
    this.furtherInformations = furtherInformations;
  }

  public Collection<ScmConstraintViolation> getViolations() {
    return unmodifiableCollection(violations);
  }

  public String getUrl() {
    return furtherInformations;
  }

  public static class Builder {
    private final Collection<ScmConstraintViolation> violations = new ArrayList<>();
    private String furtherInformations;

    public static Builder doThrow() {
      Builder builder = new Builder();
      return builder;
    }

    public Builder andThrow() {
      this.violations.clear();
      this.furtherInformations = null;
      return this;
    }

    public Builder violation(String message, String... pathElements) {
      this.violations.add(new ScmConstraintViolation(message, pathElements));
      return this;
    }

    public Builder withFurtherInformations(String furtherInformations) {
      this.furtherInformations = furtherInformations;
      return this;
    }

    public void when(boolean condition) {
      if (condition && !this.violations.isEmpty()) {
        throw new ScmConstraintViolationException(violations, furtherInformations);
      }
    }
  }

  public static class ScmConstraintViolation {
    private final String message;
    private final String path;

    private ScmConstraintViolation(String message, String... pathElements) {
      this.message = message;
      this.path = String.join(".", pathElements);
    }

    public String getMessage() {
      return message;
    }

    public String getPropertyPath() {
      return path;
    }
  }
}
