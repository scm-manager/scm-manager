/**
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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * The {@link Person} (author) of a changeset.
 *
 * @person Sebastian Sdorra
 */
@XmlRootElement(name = "person")
@XmlAccessorType(XmlAccessType.FIELD)
public class Person implements Validateable, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -4675080650527063196L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link Person}.
   * This constructor is used by JAXB.
   *
   */
  public Person() {}

  /**
   * Constructs a new {@link Person}.
   *
   *
   * @param name name of {@link Person}
   */
  public Person(String name)
  {
    this.name = name;
  }

  /**
   * Constructs a new {@link Person} with name and mail address.
   *
   *
   * @param name name of the {@link Person}
   * @param mail mail address of the {@link Person}
   */
  public Person(String name, String mail)
  {
    this.name = name;
    this.mail = mail;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Parses the given string and returns a {@link Person} object. The string
   * should be in the format "name &gt;mail&lt;". if the string contains no
   * "&gt;&lt;" the whole string is handled as the name of the {@link Person}.
   *
   *
   * @param value string representation of a {@link Person} object
   *
   * @return {@link Person} object which is generated from the given string
   */
  public static Person toPerson(String value)
  {
    Person person = null;

    if (Util.isNotEmpty(value))
    {
      String name = value;
      String mail = null;
      int s = value.indexOf('<');
      int e = value.indexOf('>');

      if ((s > 0) && (e > 0))
      {
        name = value.substring(0, s).trim();
        mail = value.substring(s + 1, e).trim();
      }

      person = new Person(name, mail);
    }

    return person;
  }

  /**
   * {@inheritDoc}
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    Person other = (Person) obj;

    return Objects.equal(name, other.name) && Objects.equal(mail, other.mail);
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, mail);
  }

  /**
   * Returns a string representation of the {@link Person} object,
   * in the format "name &gt;mail&lt;".
   *
   *
   * @return string representation of {@link Person} object
   */
  @Override
  public String toString()
  {
    String out = name;

    if (mail != null)
    {
      out = out.concat(" <").concat(mail).concat(">");
    }

    return out;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the mail address of the changeset author.
   *
   *
   * @return mail address of the changeset author
   *
   * @return
   */
  public String getMail()
  {
    return mail;
  }

  /**
   * Returns the name of the changeset author.
   *
   *
   * @return name of the changeset person
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns true if the person is valid.
   *
   *
   * @return true if the person is valid
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(name)
           && (Util.isEmpty(mail) || ValidationUtil.isMailAddressValid(mail));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the mail address of the changeset author.
   *
   *
   * @param mail mail address of the author
   */
  public void setMail(String mail)
  {
    this.mail = mail;
  }

  /**
   * Sets the name of the changeset author.
   *
   *
   * @param name name of the author
   */
  public void setName(String name)
  {
    this.name = name;
  }

  //~--- fields ---------------------------------------------------------------

  /** mail address of the person */
  private String mail;

  /**
   * name of the person
   */
  private String name;
}
