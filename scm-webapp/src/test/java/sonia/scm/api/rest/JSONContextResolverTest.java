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
package sonia.scm.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JSONContextResolver}.
 * 
 * @author Sebastian Sdorra
 */
public class JSONContextResolverTest {

  private final ObjectMapper mapper = new ObjectMapperProvider().get();

  /**
   * Tests json unmarshalling with unknown properties.
   * 
   * @throws IOException 
   */
  @Test
  public void testUnmarshalWithUnknownPropertery() throws IOException {
    SampleOne one = mapper.readValue("{\"title\": \"super title\", \"body\": \"super body\"}", SampleOne.class);
    assertEquals("super title", one.title);
  }
  
  private static class SampleOne {
    
    private String title;

    public void setTitle(String title) {
      this.title = title;
    }
    
  }
  
  /**
   * Tests json marshaling with JAXB annotations.
   * 
   * @throws JsonProcessingException 
   */
  @Test
  public void testMarshalWithJAXBAnnotations() throws JsonProcessingException {
    SampleTwo two = new SampleTwo();
    two.label = "super title";
    assertEquals("{\"title\":\"super title\"}", mapper.writeValueAsString(two));
  }
  
  /**
   * Tests json unmarshalling with JAXB annotations.
   * 
   * @throws JsonProcessingException 
   */
  @Test
  public void testUnmarshalWithJAXBAnnotations() throws IOException {
    SampleTwo two = mapper.readValue("{\"title\": \"super title\"}", SampleTwo.class);
    assertEquals("super title", two.label);
  }
  
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class SampleTwo {
    
    @XmlElement(name = "title")
    private String label;
    
  }
  /**
   * Tests json marshaling with jackson annotations.
   * 
   * @throws JsonProcessingException 
   */
  @Test
  public void testMarshalWithJacksonAnnotations() throws JsonProcessingException {
    SampleThree two = new SampleThree();
    two.setLabel("super title");
    assertEquals("{\"title\":\"super title\"}", mapper.writeValueAsString(two));
  }
  
  /**
   * Tests json unmarshalling with jackson annotations.
   * 
   * @throws JsonProcessingException 
   */
  @Test
  public void testUnmarshalWithJacksonAnnotations() throws IOException {
    SampleThree three = mapper.readValue("{\"title\": \"super title\"}", SampleThree.class);
    assertEquals("super title", three.getLabel());
  }
  
  private static class SampleThree {
    
    @JsonProperty(value = "title")
    private String label;

    public void setLabel(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
    
  }

  @Test
  public void shouldWriteDate() throws JsonProcessingException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    LocalDateTime dateTime = LocalDateTime.parse("1998-06-13 14:40", formatter);

    DateSample value = new DateSample(dateTime.toInstant(ZoneOffset.UTC));

    assertEquals("{\"date\":\"1998-06-13T14:40:00Z\"}", mapper.writeValueAsString(value));
  }

  @Data @AllArgsConstructor @NoArgsConstructor
  private static class DateSample {
    private Instant date;
  }

  @Test
  public void shouldNotWriteEmptyOptionals() throws JsonProcessingException {
    OptionalSample emptyValue = new OptionalSample(Optional.empty());
    OptionalSample presentValue = new OptionalSample(Optional.of("world"));

    assertEquals("{}", mapper.writeValueAsString(emptyValue));
    assertEquals("{\"value\":\"world\"}", mapper.writeValueAsString(presentValue));
  }

  @Test
  public void shouldReadEmptyOptionals() throws IOException {
    OptionalSample value = mapper.readValue("{}", OptionalSample.class);
    assertNotNull("Optional should not be null", value.getValue());
    assertFalse("Optional should be set as empty", value.getValue().isPresent());
  }

  @Test
  public void shouldReadNonEmptyOptionals() throws IOException {
    OptionalSample value = mapper.readValue("{\"value\":\"world\"}", OptionalSample.class);
    assertEquals("world", value.getValue().get());
  }

  @Data @AllArgsConstructor @NoArgsConstructor
  private static class OptionalSample {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Optional<String> value;
  }
}
