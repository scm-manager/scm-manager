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
