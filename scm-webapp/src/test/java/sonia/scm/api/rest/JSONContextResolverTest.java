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

package sonia.scm.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link JSONContextResolver}.
 * 
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
