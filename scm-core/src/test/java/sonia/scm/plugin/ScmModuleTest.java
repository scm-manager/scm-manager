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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Resources;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmModuleTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testUnmarshall()
  {
    URL resource = Resources.getResource("sonia/scm/plugin/module.xml");
    ScmModule module = JAXB.unmarshal(resource, ScmModule.class);

    // stupid cast to Class<?> to make hamecrest generics happy

    //J-
    assertThat(
      module.getExtensions(), 
      containsInAnyOrder(
        (Class<?>) String.class, 
        (Class<?>) Integer.class
      )
    );
    assertThat(
      module.getExtensionPoints(), 
      containsInAnyOrder(
        new ExtensionPointElement(String.class, true), 
        new ExtensionPointElement(Long.class, true), 
        new ExtensionPointElement(Integer.class, false)
      )
    );
    assertThat(
      module.getEvents(), 
      containsInAnyOrder(
        (Class<?>) String.class,
        (Class<?>) Boolean.class
      )
    );
    assertThat(
      module.getSubscribers(), 
      containsInAnyOrder(
        new SubscriberElement(Long.class, Integer.class), 
        new SubscriberElement(Double.class, Float.class)
      )
    );
    assertThat(
      module.getJaxrsProviders(), 
      containsInAnyOrder(
        (Class<?>) Integer.class,
        (Class<?>) Long.class
      )
    );
    assertThat(
      module.getJaxrsResources(), 
      containsInAnyOrder(
        (Class<?>) Float.class,
        (Class<?>) Double.class
      )
    );
    //J+
  }
}
