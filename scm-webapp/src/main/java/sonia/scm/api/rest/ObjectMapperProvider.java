package sonia.scm.api.rest;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ObjectMapperProvider implements Provider<ObjectMapper> {

  @Override
  public ObjectMapper get() {
    ObjectMapper mapper = new ObjectMapper()
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule());
    mapper.setAnnotationIntrospector(createAnnotationIntrospector());
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
    mapper.setDateFormat(new ISO8601DateFormat());
    return mapper;
  }

  private AnnotationIntrospector createAnnotationIntrospector() {
    return new AnnotationIntrospectorPair(
      new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()),
      new JacksonAnnotationIntrospector()
    );
  }
}
