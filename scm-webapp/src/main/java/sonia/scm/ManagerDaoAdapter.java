package sonia.scm;

import com.github.sdorra.ssp.PermissionCheck;
import sonia.scm.util.AssertUtil;

import java.util.function.Function;
import java.util.function.Supplier;

public class ManagerDaoAdapter<T extends ModelObject, E extends Exception> {

  private final GenericDAO<T> dao;
  private final Supplier<E> notFoundException;
  private final Function<T, E> alreadyExistsException;

  public ManagerDaoAdapter(GenericDAO<T> dao, Supplier<E> notFoundException, Function<T, E> alreadyExistsException) {
    this.dao = dao;
    this.notFoundException = notFoundException;
    this.alreadyExistsException = alreadyExistsException;
  }

  public void modify(T object, Function<T, PermissionCheck> permissionCheck, AroundHandler<T, E> beforeUpdate, AroundHandler<T, E> afterUpdate) throws E {
    String name = object.getId();

    T notModified = dao.get(name);
    if (notModified != null) {
      permissionCheck.apply(notModified).check();
      AssertUtil.assertIsValid(object);

      beforeUpdate.handle(notModified);

      object.setLastModified(System.currentTimeMillis());
      object.setCreationDate(notModified.getCreationDate());

      dao.modify(object);

      afterUpdate.handle(notModified);
    } else {
      throw notFoundException.get();
    }
  }

  public T create(T newObject, Supplier<PermissionCheck> permissionCheck, AroundHandler<T, E> beforeCreate, AroundHandler<T, E> afterCreate) throws E {
    permissionCheck.get().check();
    AssertUtil.assertIsValid(newObject);
    if (dao.contains(newObject)) {
      throw alreadyExistsException.apply(newObject);
    }
    newObject.setCreationDate(System.currentTimeMillis());
    beforeCreate.handle(newObject);
    dao.add(newObject);
    afterCreate.handle(newObject);
    return newObject;
  }

  @FunctionalInterface
  public interface AroundHandler<T extends ModelObject, E extends Exception> {
    void handle(T notModified) throws E;
  }
}
