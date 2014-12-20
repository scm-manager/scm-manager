/**
 * Copyright (c) 2014, Sebastian Sdorra
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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class PrioritiesTest
{

  /**
   * Constructs ...
   *
   */
  public PrioritiesTest() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testGetPriority()
  {
    assertEquals(Priorities.POST_AUTHENTICATION,
      Priorities.getPriority(A.class));
    assertEquals(Priorities.DEFAULT, Priorities.getPriority(D.class));
  }

  /**
   * Method description
   *
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testSort()
  {
    List<Class<?>> cls = ImmutableList.of(A.class, B.class, C.class, D.class);

    cls = Priorities.sort(cls);
    assertThat(cls, contains(B.class, C.class, A.class, D.class));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/12/20
   * @author         Enter your name here...
   */
  @Priority(Priorities.POST_AUTHENTICATION)
  public static class A {}


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/12/20
   * @author         Enter your name here...
   */
  @Priority(Priorities.PRE_AUTHENTICATION)
  public static class B {}


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/12/20
   * @author         Enter your name here...
   */
  @Priority(Priorities.AUTHENTICATION)
  public static class C {}


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/12/20
   * @author         Enter your name here...
   */
  public static class D {}
}
