/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.util;

import jakarta.annotation.Nullable;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.repository.Person;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.EMail;
import sonia.scm.user.User;

/**
 * Contains convenience methods to manage {@link CommandWithAuthor} classes.
 */
public class AuthorUtil {

  /**
   * @see AuthorUtil#createAuthorFromSubject(EMail)
   * @param request {@link CommandWithAuthor}
   */
  public static void setAuthorIfNotAvailable(CommandWithAuthor request) {
    setAuthorIfNotAvailable(request, null);
  }

  /**
   * @see AuthorUtil#createAuthorFromSubject(EMail)
   * @param request {@link CommandWithAuthor}
   * @param eMail {@link EMail}
   */
  public static void setAuthorIfNotAvailable(CommandWithAuthor request, @Nullable EMail eMail) {
    if (request.getAuthor() == null) {
      request.setAuthor(createAuthorFromSubject(eMail));
    }
  }

  /**
   * Depending on the mail input, the {@link Person} is either created by the given nullable {@link EMail}
   * or the information from {@link DisplayUser} if the mail remains null.
   * @param user {@link DisplayUser}
   * @param eMail (nullable) {@link EMail}
   * @return {@link Person}
   */
  public static Person createAuthorWithMailFallback(DisplayUser user, @Nullable EMail eMail) {
    String name = user.getDisplayName();
    String mailAddress = eMail != null ? eMail.getMailOrFallback(user) : user.getMail();
    return new Person(name, mailAddress);
  }

  /**
   * Creates an author from the Apache Shiro {@link Subject} given by the {@link SecurityUtils}.
   * @param eMail {@link EMail}
   * @return {@link Person}
   */
  private static Person createAuthorFromSubject(@Nullable EMail eMail) {
    Subject subject = SecurityUtils.getSubject();
    User user = subject.getPrincipals().oneByType(User.class);
    return createAuthorWithMailFallback(DisplayUser.from(user), eMail);
  }

  /**
   * Command whose execution includes an author as a {@link Person}.
   */
  public interface CommandWithAuthor {
    Person getAuthor();

    void setAuthor(Person person);
  }
}
