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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginConditionTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testArchIsSupported()
  {
    assertTrue(new PluginCondition(null, null, "32").isSupported(null, null,
                                   "32"));
    assertFalse(new PluginCondition(null, null, "32").isSupported(null, null,
                                    "64"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsOsSupported()
  {
    assertTrue(new PluginCondition(null, Arrays.asList("unix"),
                                   null).isSupported(null, "linux", null));
    assertTrue(new PluginCondition(null, Arrays.asList("win"),
                                   null).isSupported(null, "Windows 2000",
                                     null));
    assertFalse(new PluginCondition(null, Arrays.asList("win"),
                                    null).isSupported(null, "Mac OS X", null));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsSupported()
  {
    assertTrue(new PluginCondition("1.2", Arrays.asList("Mac"),
                                   "64").isSupported("1.4", "Mac OS X", "64"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testVersionIsSupported()
  {
    assertTrue(new PluginCondition("1.1", null, null).isSupported("1.2", null,
                                   null));
    assertTrue(new PluginCondition("1.0", null,
                                   null).isSupported("1.1-SNAPSHOT", null,
                                     null));
  }
}
