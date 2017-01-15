/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Serializable;

/**
 * Unit tests for {@link DeepCopy}.
 * 
 * @author Sebastian Sdorra
 */
public class DeepCopyTest {

  /**
   * Tests {@link DeepCopy#copy(Object)}.
   *
   * @throws IOException
   */
  @Test
  public void testDeepCopy() throws IOException {
    Person orig = new Person("Tricia", "McMillan", new Address("Magrathea", "Mainstreet 3"));
    Person copy = DeepCopy.copy(orig);

    assertNotSame(orig, copy);
    assertEquals(orig, copy);
  }

  /**
   * Tests {@link DeepCopy#copy(Object)} with a non serializable object.
   *
   * @throws IOException
   */
  @Test(expected = IOException.class)
  public void testDeepCopyNonSerializable() throws IOException {
    DeepCopy.copy(new NonSerializable());
  }

  //~--- inner classes --------------------------------------------------------

  private static class Address implements Serializable {

    private static final long serialVersionUID = 3200816222378286310L;
    
    public Address() {}

    public Address(String city, String street) {
      this.city = city;
      this.street = street;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final Address other = (Address) obj;

      return Objects.equal(city, other.city)
        && Objects.equal(street, other.street);
    }
    
    @Override
    public int hashCode() {
      return Objects.hashCode(city, street);
    }

    //~--- get methods --------------------------------------------------------

    public String getCity() {
      return city;
    }

    public String getStreet() {
      return street;
    }

    //~--- fields -------------------------------------------------------------

    private String city;

    private String street;
  }

  private static class NonSerializable {}

  private static class Person implements Serializable {

    private static final long serialVersionUID = -2098386757802626539L;

    //~--- constructors -------------------------------------------------------

    public Person() {}

    public Person(String firstname, String lastname, Address address) {
      this.firstname = firstname;
      this.lastname = lastname;
      this.address = address;
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final Person other = (Person) obj;

      return Objects.equal(firstname, other.firstname)
        && Objects.equal(lastname, other.lastname)
        && Objects.equal(address, other.address);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(firstname, lastname, address);
    }

    //~--- get methods --------------------------------------------------------

    public Address getAddress() {
      return address;
    }
    
    public String getFirstname() {
      return firstname;
    }

    public String getLastname() {
      return lastname;
    }

    //~--- fields -------------------------------------------------------------

    private Address address;
    
    private String firstname;

    private String lastname;
  }
}
