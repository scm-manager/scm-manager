package sonia.scm.api.v2.resources;

import com.google.inject.binder.AnnotatedBindingBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperModuleTest {

  @Test
  public void shouldBindToClassesWithDefaultConstructorOnly() {
    AnnotatedBindingBuilder binding = mock(AnnotatedBindingBuilder.class);
    ArgumentCaptor<Class> captor = ArgumentCaptor.forClass(Class.class);
    when(binding.to(captor.capture())).thenReturn(null);
    new MapperModule() {
      @Override
      protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return binding;
      }
    }.configure();
    captor.getAllValues().forEach(this::verifyClassCanBeInstantiated);
  }

  private <T> T verifyClassCanBeInstantiated(Class<T> c) {
    try {
      return c.getConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
