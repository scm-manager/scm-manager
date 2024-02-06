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
