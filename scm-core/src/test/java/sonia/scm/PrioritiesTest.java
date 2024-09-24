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

package sonia.scm;


import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import java.util.List;


public class PrioritiesTest
{

    /** authentication priority */
  public static final int AUTHENTICATION = 5000;
  
    /** post authentication priority */
  public static final int POST_AUTHENTICATION = 5500;
  
    /** pre authentication priority */
  public static final int PRE_AUTHENTICATION = 4500;

  
  public PrioritiesTest() {}


   @Test
  public void testGetPriority()
  {
    assertEquals(POST_AUTHENTICATION,
      Priorities.getPriority(A.class));
    assertEquals(Priorities.DEFAULT, Priorities.getPriority(D.class));
  }

   @Test
  @SuppressWarnings("unchecked")
  public void testSort()
  {
    List<Class<?>> cls = ImmutableList.of(A.class, B.class, C.class, D.class);

    cls = Priorities.sort(cls);
    assertThat(cls, contains(B.class, C.class, A.class, D.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldSortInstances()
  {
    A a = new A();
    B b = new B();
    C c = new C();
    D d = new D();

    List<?> instances = ImmutableList.of(a, b, c, d);

    instances = Priorities.sortInstances(instances);
    assertThat(instances, contains(b, c, a, d));
  }




  @Priority(POST_AUTHENTICATION)
  public static class A {}



  @Priority(PRE_AUTHENTICATION)
  public static class B {}



  @Priority(AUTHENTICATION)
  public static class C {}



  public static class D {}
}
