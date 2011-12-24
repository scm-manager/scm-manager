/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
      int s = value.indexOf("<");
      int e = value.indexOf(">");

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

  /** name of the person */
  private String mail;

  /** mail address of the person */
  private String name;
}
