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


import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import jakarta.xml.bind.JAXB;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;


public class ScmModuleTest
{

   @Test
  @SuppressWarnings("unchecked")
  public void testUnmarshall()
  {
    URL resource = Resources.getResource("sonia/scm/plugin/module.xml");
    ScmModule module = JAXB.unmarshal(resource, ScmModule.class);

    // stupid cast to Class<?> to make hamecrest generics happy

    //J-
    assertThat(
      Iterables.transform(module.getExtensions(), ClassElement::getClazz),
      containsInAnyOrder(
        String.class.getName(),
        Integer.class.getName()
      )
    );
    assertThat(
      module.getExtensionPoints(),
      containsInAnyOrder(
        new ExtensionPointElement(String.class, "ext01", true, true),
        new ExtensionPointElement(Long.class, "ext02", true, true),
        new ExtensionPointElement(Integer.class, "ext03", false, true)
      )
    );
    assertThat(
      module.getEvents().iterator().next(),
      instanceOf(ClassElement.class)
    );
    assertThat(
      module.getSubscribers(),
      containsInAnyOrder(
        new SubscriberElement(Long.class, Integer.class, "sub01"),
        new SubscriberElement(Double.class, Float.class, "sub02")
      )
    );
    assertThat(
      module.getRestProviders().iterator().next(),
      instanceOf(ClassElement.class)
    );
    assertThat(
      module.getRestResources().iterator().next(),
      instanceOf(ClassElement.class)
    );
    //J+
  }
}
