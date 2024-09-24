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
