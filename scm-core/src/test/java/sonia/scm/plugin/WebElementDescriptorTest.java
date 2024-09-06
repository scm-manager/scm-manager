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

import jakarta.xml.bind.JAXB;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class WebElementDescriptorTest {

  private static final String XML_1 = String.join("\n",
    "<web-element>",
    "  <class>com.hitchhiker.SpaceShip</class>",
    "  <value>/space/*</value>",
    "  <regex>false</regex>",
    "  <morePatterns>/ship/*,/star/*</morePatterns>",
    "  <requires>scm-magrathea-plugin</requires>",
    "  <requires>scm-earth-plugin</requires>",
    "</web-element>"
  );
  private static final String XML_2 = String.join("\n",
    "<web-element>",
    "  <class>com.hitchhiker.SpaceShip</class>",
    "  <value>/space/.*</value>",
    "  <regex>true</regex>",
    "</web-element>"
  );

  @Test
  void shouldUnmarshall() {
    WebElementDescriptor descriptor = unmarshal(XML_1);
    assertThat(descriptor.getClazz()).isEqualTo("com.hitchhiker.SpaceShip");
    assertThat(descriptor.getPattern()).isEqualTo("/space/*");
    assertThat(descriptor.getMorePatterns()).containsExactly("/ship/*", "/star/*");
    assertThat(descriptor.isRegex()).isFalse();
    assertThat(descriptor.getRequires()).containsExactlyInAnyOrder("scm-magrathea-plugin", "scm-earth-plugin");
  }

  @Test
  void shouldUnmarshallWithoutMorePatterns() {
    WebElementDescriptor descriptor = unmarshal(XML_2);
    assertThat(descriptor.getClazz()).isEqualTo("com.hitchhiker.SpaceShip");
    assertThat(descriptor.getMorePatterns()).isEmpty();
    assertThat(descriptor.isRegex()).isTrue();
  }

  private WebElementDescriptor unmarshal(String content) {
    return JAXB.unmarshal(new StringReader(content), WebElementDescriptor.class);
  }

}
