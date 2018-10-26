package sonia.scm.api.v2.resources;

import javax.inject.Named;
import javax.validation.ParameterNameProvider;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Provider
public class ResourceParameterNameProvider implements ParameterNameProvider {
  @Override
  public List<String> getParameterNames(Constructor<?> constructor) {
    return Arrays.stream(constructor.getParameters()).map(this::getParameterName).collect(Collectors.toList());
  }

  @Override
  public List<String> getParameterNames(Method method) {
    return Arrays.stream(method.getParameters()).map(this::getParameterName).collect(Collectors.toList());
  }

  private String getParameterName(Parameter parameter) {
    return Optional.ofNullable(parameter.getAnnotation(Named.class)).map(Named::value).orElse(parameter.getName());
  }
}
