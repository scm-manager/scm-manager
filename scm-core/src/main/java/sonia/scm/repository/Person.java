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
