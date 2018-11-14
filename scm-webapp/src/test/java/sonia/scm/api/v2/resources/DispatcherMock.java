package sonia.scm.api.v2.resources;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import sonia.scm.api.rest.AlreadyExistsExceptionMapper;
import sonia.scm.api.rest.AuthorizationExceptionMapper;
import sonia.scm.api.rest.ConcurrentModificationExceptionMapper;
import sonia.scm.api.v2.NotFoundExceptionMapper;
import sonia.scm.api.v2.NotSupportedFeatureExceptionMapper;

public class DispatcherMock {
  public static Dispatcher createDispatcher(Object resource) {
    Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);
    ExceptionWithContextToErrorDtoMapperImpl mapper = new ExceptionWithContextToErrorDtoMapperImpl();
    dispatcher.getProviderFactory().register(new NotFoundExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new AlreadyExistsExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new ConcurrentModificationExceptionMapper(mapper));
    dispatcher.getProviderFactory().registerProvider(AuthorizationExceptionMapper.class);
    dispatcher.getProviderFactory().register(new InternalRepositoryExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new ChangePasswordNotAllowedExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new InvalidPasswordExceptionMapper(mapper));
    dispatcher.getProviderFactory().register(new NotSupportedFeatureExceptionMapper(mapper));
    return dispatcher;
  }
}
