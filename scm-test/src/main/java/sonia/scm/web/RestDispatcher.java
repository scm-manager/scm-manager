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

package sonia.scm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import sonia.scm.AlreadyExistsException;
import sonia.scm.BadRequestException;
import sonia.scm.BranchAlreadyExistsException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.ConflictException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

public class RestDispatcher {

  private final Dispatcher dispatcher;
  private final EnhanceableExceptionMapper exceptionMapper;

  public RestDispatcher() {
    dispatcher = MockDispatcherFactory.createDispatcher();
    exceptionMapper = new EnhanceableExceptionMapper();
    dispatcher.getProviderFactory().register(exceptionMapper);
    dispatcher.getProviderFactory().registerProviderInstance(new JacksonProducer());
  }

  public void addSingletonResource(Object resource) {
    dispatcher.getRegistry().addSingletonResource(resource);
  }

  public void invoke(HttpRequest in, HttpResponse response) {
    dispatcher.invoke(in, response);
  }

  public void registerException(Class<? extends RuntimeException> exceptionClass, Status status) {
    exceptionMapper.registerException(exceptionClass, status);
  }

  public ResteasyProviderFactory getProviderFactory() {
    return dispatcher.getProviderFactory();
  }

  public <T> void putDefaultContextObject(Class<T> clazz, T object) {
    dispatcher.getDefaultContextObjects().put(clazz, object);
  }

  private static class EnhanceableExceptionMapper implements ExceptionMapper<Exception> {

    private final Map<Class<? extends RuntimeException>, Integer> statusCodes = new HashMap<>();

    public EnhanceableExceptionMapper() {
      registerException(NotFoundException.class, Status.NOT_FOUND);
      registerException(AlreadyExistsException.class, Status.CONFLICT);
      registerException(BranchAlreadyExistsException.class, Status.CONFLICT);
      registerException(ConcurrentModificationException.class, Status.CONFLICT);
      registerException(ConflictException.class, Status.CONFLICT);
      registerException(UnauthorizedException.class, Status.FORBIDDEN);
      registerException(AuthorizationException.class, Status.FORBIDDEN);
      registerException(AuthenticationException.class, Status.UNAUTHORIZED);
      registerException(BadRequestException.class, Status.BAD_REQUEST);
      registerException(ScmConstraintViolationException.class, Status.BAD_REQUEST);
    }

    private void registerException(Class<? extends RuntimeException> exceptionClass, Status status) {
      statusCodes.put(exceptionClass, status.getStatusCode());
    }

    @Override
    public Response toResponse(Exception e) {
      return Response.status(getStatus(e)).entity(e.getMessage()).build();
    }

    private Integer getStatus(Exception ex) {
      if (ex instanceof WebApplicationException) {
        return ((WebApplicationException) ex).getResponse().getStatus();
      }
      return statusCodes
        .entrySet()
        .stream()
        .filter(e -> e.getKey().isAssignableFrom(ex.getClass()))
        .map(Map.Entry::getValue)
        .findAny()
        .orElseGet(() -> handleUnknownException(ex));
    }

    private Integer handleUnknownException(Exception ex) {
      System.err.println("got unknown exception in rest api test");
      ex.printStackTrace();
      return 500;
    }
  }

  @Provider
  @Produces("application/*+json")
  public static class JacksonProducer implements ContextResolver<ObjectMapper> {
    public JacksonProducer() {
      this.json
        = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType) {
      return json;
    }

    private final ObjectMapper json;
  }
}
