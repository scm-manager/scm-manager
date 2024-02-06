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
    
package sonia.scm.plugin;


import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;


public class PluginConditionTest
{

  @Test
  public void testEmptyShouldBeSupported() {
    assertTrue(new PluginCondition().isSupported());
  }

   @Test
  public void testArchIsSupported()
  {
    assertTrue(new PluginCondition(null, null, "32").isSupported(null, null,
                                   "32"));
    assertFalse(new PluginCondition(null, null, "32").isSupported(null, null,
                                    "64"));
  }

   @Test
  public void testIsOsSupported()
  {
    assertTrue(new PluginCondition(null, Arrays.asList("linux"),
                                   null).isSupported(null, "linux", null));
    assertTrue(new PluginCondition(null, Arrays.asList("unix"),
                                   null).isSupported(null, "Mac OS X", null));
    assertTrue(new PluginCondition(null, Arrays.asList("unix"),
                                   null).isSupported(null, "Solaris", null));
    assertTrue(new PluginCondition(null, Arrays.asList("posix"),
                                   null).isSupported(null, "Linux", null));
    assertTrue(new PluginCondition(null, Arrays.asList("win"),
                                   null).isSupported(null, "Windows 2000",
                                     null));
    assertFalse(new PluginCondition(null, Arrays.asList("win"),
                                    null).isSupported(null, "Mac OS X", null));
  }

   @Test
  public void testIsSupported()
  {
    assertTrue(new PluginCondition("1.2", Arrays.asList("Mac"),
                                   "64").isSupported("1.4", "Mac OS X", "64"));
  }

   @Test
  public void testVersionIsSupported()
  {
    assertTrue(new PluginCondition("1.1", null, null).isSupported("1.2", null,
                                   null));
    assertTrue(new PluginCondition("1.0", null,
                                   null).isSupported("1.1-SNAPSHOT", null,
                                     null));
    assertTrue(new PluginCondition("1.1", null, null).isSupported("1.1", null,
                                   null));
  }
}
