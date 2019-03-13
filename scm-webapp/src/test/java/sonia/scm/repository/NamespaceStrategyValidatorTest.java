package sonia.scm.repository;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import sonia.scm.ScmConstraintViolationException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceStrategyValidatorTest {

  @Test
  void shouldThrowConstraintValidationException() {
    NamespaceStrategyValidator validator = new NamespaceStrategyValidator(Collections.emptySet());
    assertThrows(ScmConstraintViolationException.class, () -> validator.check("AwesomeStrategy"));
  }

  @Test
  void shouldDoNotThrowAnException() {
    NamespaceStrategyValidator validator = new NamespaceStrategyValidator(Sets.newHashSet(new AwesomeStrategy()));
    validator.check("AwesomeStrategy");
  }

  public static class AwesomeStrategy implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return null;
    }
  }

}
