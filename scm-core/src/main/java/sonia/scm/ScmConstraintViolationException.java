/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

/**
 * Use this exception to handle invalid input values that cannot be handled using
 * <a href="https://docs.oracle.com/javaee/7/tutorial/bean-validation001.htm#GIRCZ">JEE bean validation</a>.
 * Use the {@link Builder} to conditionally create a new exception:
 * <pre>
 * Builder
 *   .doThrow()
 *   .violation("name or alias must not be empty if not anonymous", "myParameter", "name")
 *   .violation("name or alias must not be empty if not anonymous", "myParameter", "alias")
 *   .when(myParameter.getName() == null &amp;&amp; myParameter.getAlias() == null &amp;&amp; !myParameter.isAnonymous())
 *   .andThrow()
 *   .violation("name must be empty if anonymous", "myParameter", "name")
 *   .when(myParameter.getName() != null &amp;&amp; myParameter.isAnonymous());
 * </pre>
 * Mind that using this way you do not have to use if-else constructs.
 */
public class ScmConstraintViolationException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = 6904534307450229887L;

  private final Collection<ScmConstraintViolation> violations;

  private final String furtherInformation;

  private ScmConstraintViolationException(Collection<ScmConstraintViolation> violations, String furtherInformation) {
    this.violations = violations;
    this.furtherInformation = furtherInformation;
  }

  /**
   * The violations that caused this exception.
   */
  public Collection<ScmConstraintViolation> getViolations() {
    return unmodifiableCollection(violations);
  }

  /**
   * An optional URL for more information about this constraint violation.
   */
  public String getUrl() {
    return furtherInformation;
  }

  /**
   * Builder to conditionally create constraint violations.
   */
  public static class Builder {
    private final Collection<ScmConstraintViolation> violations = new ArrayList<>();
    private String furtherInformation;

    /**
     * Use this to create a new builder instance.
     */
    public static Builder doThrow() {
      return new Builder();
    }

    /**
     * Resets this builder to check for further violations.
     * @return this builder instance.
     */
    public Builder andThrow() {
      this.violations.clear();
      this.furtherInformation = null;
      return this;
    }

    /**
     * Describes the violation with a custom message and the affected property. When more than one property is affected,
     * you can call this method multiple times.
     * @param message The message describing the violation.
     * @param pathElements The affected property denoted by the path to reach this property,
     *                    e.g. "someParameter", "complexProperty", "attribute"
     * @return this builder instance.
     */
    public Builder violation(String message, String... pathElements) {
      this.violations.add(new ScmConstraintViolation(message, pathElements));
      return this;
    }

    /**
     * Use this to specify a URL with further information about this violation and hints how to solve this.
     * This is optional.
     * @return this builder instance.
     */
    public Builder withFurtherInformation(String furtherInformation) {
      this.furtherInformation = furtherInformation;
      return this;
    }

    /**
     * When the given condition is <code>true</code>, an exception will be thrown. Otherwise, this simply resets this
     * builder and does nothing else.
     * @param condition The condition that indicates a violation of this constraint.
     * @return this builder instance.
     */
    public Builder when(boolean condition) {
      if (condition && !this.violations.isEmpty()) {
        throw new ScmConstraintViolationException(violations, furtherInformation);
      }
      return andThrow();
    }
  }

  /**
   * A single constraint violation.
   */
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
