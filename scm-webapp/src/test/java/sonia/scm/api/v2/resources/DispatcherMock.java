package sonia.scm.api.v2.resources;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import sonia.scm.api.rest.AlreadyExistsExceptionMapper;
import sonia.scm.api.rest.AuthorizationExceptionMapper;

public class DispatcherMock {
  public static Dispatcher createDispatcher(Object resource) {
    Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);
    dispatcher.getProviderFactory().registerProvider(NotFoundExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(AlreadyExistsExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(AuthorizationExceptionMapper.class);
    return dispatcher;
  }
}
