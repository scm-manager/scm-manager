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

package sonia.scm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.AlreadyExistsException;
import sonia.scm.BadRequestException;
import sonia.scm.BranchAlreadyExistsException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

public class RestDispatcher {

  private static final Logger LOG = LoggerFactory.getLogger(RestDispatcher.class);

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
      LOG.info("got unknown exception in rest api test", ex);
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
