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

package sonia.scm.xml;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FilterInvalidCharXMLStreamWriterTest {

  private static List<Integer> invalidXmlChars;

  @BeforeAll
  static void setupInvalidChars() {
    invalidXmlChars = new ArrayList<>();

    for(int i = 0; i < 0x110000; ++i) {
      if(i == 0x9 || i == 0xA || i == 0xD || (i >= 0x20 && i <= 0xD7FF) || (i >= 0xE000 && i <= 0xFFFD) || (i >= 0x10000 && i <= 0x10FFFF)) {
        continue;
      }

      invalidXmlChars.add(i);
    }
  }

  @Test
  void shouldWriteDefaultStartOfXml() throws XMLStreamException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
      writer.writeStartDocument();
    }

    assertThat(outputStream.toString()).isEqualTo("<?xml version=\"1.0\" ?>");
  }

  @Test
  void shouldWriteStartOfXmlWithVersion() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartDocument(addInvalidChars("1.1", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<?xml version=\"1.1\"?>");
    }
  }

  @Test
  void shouldWriteStartOfXmlWithVersionAndEncoding() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartDocument(addInvalidChars("UTF-8", invalidChar), addInvalidChars("1.1", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<?xml version=\"1.1\" encoding=\"UTF-8\"?>");
    }
  }

  @Test
  void shouldWriteStartElement() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartElement(addInvalidChars("Root", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<Root");
    }
  }

  @Test
  void shouldWriteStartElementWithNamespace() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.setPrefix(
                addInvalidChars("pre", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar)
        );

        writer.writeStartElement(
                addInvalidChars("https://www.test-namespace.org/", invalidChar),
                addInvalidChars("Root", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<pre:Root");
    }
  }

  @Test
  void shouldWriteStartElementWithNamespaceAndPrefix() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartElement(
                addInvalidChars("other_pre", invalidChar),
                addInvalidChars("Root", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<other_pre:Root");
    }
  }

  @Test
  void shouldWriteEmptyElement() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeEmptyElement(addInvalidChars("Root", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<Root");
    }
  }

  @Test
  void shouldWriteEmptyElementWithNamespace() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.setPrefix(
                addInvalidChars("pre", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar)
        );

        writer.writeEmptyElement(
                addInvalidChars("https://www.test-namespace.org/", invalidChar),
                addInvalidChars("Root", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<pre:Root");
    }
  }

  @Test
  void shouldWriteEmptyElementWithNamespaceAndPrefix() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeEmptyElement(
                addInvalidChars("other_pre", invalidChar),
                addInvalidChars("Root", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<other_pre:Root");
    }
  }

  @Test
  void shouldWriteEndOfElement() throws XMLStreamException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
      writer.writeStartElement("Root");
      writer.writeEndElement();
    }

    assertThat(outputStream.toString()).isEqualTo("<Root></Root>");
  }

  @Test
  void shouldWriteEndOfDocument() throws XMLStreamException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
      writer.writeStartElement("Root");
      writer.writeStartElement("Child");
      writer.writeEndDocument();
    }

    assertThat(outputStream.toString()).isEqualTo("<Root><Child></Child></Root>");
  }

  @Test
  void shouldWriteAttribute() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartElement("Root");
        writer.writeAttribute(addInvalidChars("attribute", invalidChar), addInvalidChars("value", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<Root attribute=\"value\"");
    }
  }

  @Test
  void shouldWriteAttributeWithNamespace() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.setPrefix(
                addInvalidChars("pre", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar)
        );
        writer.writeStartElement("Root");
        writer.writeAttribute(
                addInvalidChars("https://www.test-namespace.org/", invalidChar),
                addInvalidChars("attribute", invalidChar),
                addInvalidChars("value", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<Root pre:attribute=\"value\"");
    }
  }

  @Test
  void shouldWriteAttributeWithNamespaceAndPrefix() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartElement("Root");
        writer.writeAttribute(
                addInvalidChars("other_pre", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar),
                addInvalidChars("attribute", invalidChar),
                addInvalidChars("value", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<Root other_pre:attribute=\"value\"");
    }
  }

  @Test
  void shouldWriteNamespace() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartElement("Root");
        writer.writeNamespace(
                addInvalidChars("pre", invalidChar),
                addInvalidChars("https://www.test-namespace.org/", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<Root xmlns:pre=\"https://www.test-namespace.org/\"");
    }
  }

  @Test
  void shouldWriteDefaultNamespace() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeStartElement("Root");
        writer.writeDefaultNamespace(addInvalidChars("https://www.test-namespace.org/", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<Root xmlns=\"https://www.test-namespace.org/\"");
    }
  }

  @Test
  void shouldWriteComment() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeComment(addInvalidChars("Comment", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<!--Comment-->");
    }
  }

  @Test
  void shouldWriteProcessingInstruction() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeProcessingInstruction(addInvalidChars("Target", invalidChar));
      }

      assertThat(outputStream.toString()).isEqualTo("<?Target?>");
    }
  }

  @Test
  void shouldWriteProcessingInstructionWithData() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeProcessingInstruction(
                addInvalidChars("Target", invalidChar),
                addInvalidChars("InstructionData", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<?Target InstructionData?>");
    }
  }

  @Test
  void shouldWriteCData() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeCData(
                addInvalidChars("Data", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<![CDATA[Data]]>");
    }
  }

  @Test
  void shouldWriteDtd() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeDTD(
                addInvalidChars("<!DOCTYPE>", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("<!DOCTYPE>");
    }
  }

  @Test
  void shouldWriteEntityRef() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeEntityRef(
                addInvalidChars("Name", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("&Name;");
    }
  }

  @Test
  void shouldWriteCharactersFromString() throws XMLStreamException {
    for (int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        writer.writeCharacters(
                addInvalidChars("&<Some Random String>&", invalidChar)
        );
      }

      assertThat(outputStream.toString()).isEqualTo("&amp;&lt;Some Random String&gt;&amp;");
    }
  }

  @ParameterizedTest
  @CsvSource({"test,0,4,test", "test,1,2,es"})
  void shouldWriteCharactersFromBuffer(String text, String start, String len, String expectedResult) throws XMLStreamException {
    char[] chars = text.toCharArray();
    int startIndex = Integer.parseInt(start);
    int length = Integer.parseInt(len);


    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
      writer.writeCharacters(chars, startIndex, length);
    }

    assertThat(outputStream.toString()).isEqualTo(expectedResult);
  }

  @Test
  void shouldWriteCharactersFromBuffer() throws XMLStreamException {
    for(int invalidChar : invalidXmlChars) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try (FilterInvalidCharXMLStreamWriter writer = new FilterInvalidCharXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream))) {
        char[] unfilteredChars = addInvalidChars("Test", invalidChar).toCharArray();
        writer.writeCharacters(unfilteredChars, 0, unfilteredChars.length);
      }

      assertThat(outputStream.toString()).isEqualTo("Test");
    }
  }

  private String addInvalidChars(String input, int invalidChar) {
    StringBuilder sb = new StringBuilder();
    sb.appendCodePoint(invalidChar);
    sb.append(input);
    sb.appendCodePoint(invalidChar);

    return sb.toString();
  }
}
