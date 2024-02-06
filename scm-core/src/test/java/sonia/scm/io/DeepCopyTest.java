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
    
package sonia.scm.io;


import com.google.common.base.Objects;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit tests for {@link DeepCopy}.
 * 
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

    

    public Person() {}

    public Person(String firstname, String lastname, Address address) {
      this.firstname = firstname;
      this.lastname = lastname;
      this.address = address;
    }

    

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
