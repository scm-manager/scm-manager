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
