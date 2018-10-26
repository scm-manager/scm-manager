package sonia.scm.api.v2.resources;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import sonia.scm.api.rest.AlreadyExistsExceptionMapper;
import sonia.scm.api.rest.AuthorizationExceptionMapper;
import sonia.scm.api.rest.ConcurrentModificationExceptionMapper;
import sonia.scm.api.rest.IllegalArgumentExceptionMapper;
import sonia.scm.api.v2.NotFoundExceptionMapper;

public class DispatcherMock {
  public static Dispatcher createDispatcher(Object resource) {
    Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);
    ExceptionWithContextToErrorDtoMapperImpl mapper = new ExceptionWithContextToErrorDtoMapperImpl();
    dispatcher.getProviderFactory().register(new NotFoundExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new AlreadyExistsExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new ConcurrentModificationExceptionMapper(mapper));
    dispatcher.getProviderFactory().registerProvider(AuthorizationExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(InternalRepositoryExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(ChangePasswordNotAllowedExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(InvalidPasswordExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(IllegalArgumentExceptionMapper.class);
    return dispatcher;
  }
}
