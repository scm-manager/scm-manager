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

package sonia.scm.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import java.io.Serializable;

/**
 * The {@link Person} (author) of a changeset.
 *
 * @person Sebastian Sdorra
 */
@EqualsAndHashCode
@Getter
@Setter
public class Person implements Validateable, Serializable {

  private static final long serialVersionUID = -4675080650527063196L;

  private String mail;

  private String name;

  public Person() {
  }

  public Person(String name) {
    this.name = name;
  }

  public Person(String name, String mail) {
    this.name = name;
    this.mail = mail;
  }

  /**
   * Parses the given string and returns a {@link Person} object. The string
   * should be in the format "name &gt;mail&lt;". if the string contains no
   * "&gt;&lt;" the whole string is handled as the name of the {@link Person}.
   *
   * @param value string representation of a {@link Person} object
   * @return {@link Person} object which is generated from the given string
   */
  public static Person toPerson(String value) {
    Person person = null;

    if (Util.isNotEmpty(value)) {
      String name = value;
      String mail = null;
      int s = value.indexOf('<');
      int e = value.indexOf('>');

      if ((s > 0) && (e > 0)) {
        name = value.substring(0, s).trim();
        mail = value.substring(s + 1, e).trim();
      }

      person = new Person(name, mail);
    }

    return person;
  }

  /**
   * Returns a string representation of the {@link Person} object,
   * in the format "name &gt;mail&lt;".
   */
  @Override
  public String toString() {
    String out = name;

    if (mail != null) {
      out = out.concat(" <").concat(mail).concat(">");
    }

    return out;
  }

  /**
   * Returns the mail address of the changeset author.
   */
  public String getMail() {
    return mail;
  }

  /**
   * Returns the name of the changeset author.
   */
  public String getName() {
    return name;
  }


  /**
   * Returns true if the person is valid.
   */
  @Override
  public boolean isValid() {
    return Util.isNotEmpty(name)
      && (Util.isEmpty(mail) || ValidationUtil.isMailAddressValid(mail));
  }
}
