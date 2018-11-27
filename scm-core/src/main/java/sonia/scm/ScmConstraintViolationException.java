package sonia.scm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

public class ScmConstraintViolationException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = 6904534307450229887L;

  private final Collection<ScmConstraintViolation> violations;

  private final String furtherInformation;

  private ScmConstraintViolationException(Collection<ScmConstraintViolation> violations, String furtherInformation) {
    this.violations = violations;
    this.furtherInformation = furtherInformation;
  }

  public Collection<ScmConstraintViolation> getViolations() {
    return unmodifiableCollection(violations);
  }

  public String getUrl() {
    return furtherInformation;
  }

  public static class Builder {
    private final Collection<ScmConstraintViolation> violations = new ArrayList<>();
    private String furtherInformation;

    public static Builder doThrow() {
      Builder builder = new Builder();
      return builder;
    }

    public Builder andThrow() {
      this.violations.clear();
      this.furtherInformation = null;
      return this;
    }

    public Builder violation(String message, String... pathElements) {
      this.violations.add(new ScmConstraintViolation(message, pathElements));
      return this;
    }

    public Builder withFurtherInformation(String furtherInformation) {
      this.furtherInformation = furtherInformation;
      return this;
    }

    public void when(boolean condition) {
      if (condition && !this.violations.isEmpty()) {
        throw new ScmConstraintViolationException(violations, furtherInformation);
      }
    }
  }

  public static class ScmConstraintViolation implements Serializable {

    private static final long serialVersionUID = -6900317468157084538L;

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
