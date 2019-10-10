package sonia.scm.repository.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.repository.Person;
import sonia.scm.user.User;

public class AuthorUtil {

  public static void setAuthorIfNotAvailable(CommandWithAuthor request) {
    if (request.getAuthor() == null) {
      request.setAuthor(createAuthorFromSubject());
    }
  }

  private static Person createAuthorFromSubject() {
    Subject subject = SecurityUtils.getSubject();
    User user = subject.getPrincipals().oneByType(User.class);
    String name = user.getDisplayName();
    String email = user.getMail();
    return new Person(name, email);
  }

  public interface CommandWithAuthor {
    Person getAuthor();

    void setAuthor(Person person);
  }
}
