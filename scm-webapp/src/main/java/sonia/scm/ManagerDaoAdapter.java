package sonia.scm;

import com.github.sdorra.ssp.PermissionCheck;
import sonia.scm.util.AssertUtil;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ManagerDaoAdapter<T extends ModelObject, E extends Exception> {

  private final GenericDAO<T> dao;
  private final Function<T, E> notFoundException;
  private final Function<T, E> alreadyExistsException;

  public ManagerDaoAdapter(GenericDAO<T> dao, Function<T, E> notFoundException, Function<T, E> alreadyExistsException) {
    this.dao = dao;
    this.notFoundException = notFoundException;
    this.alreadyExistsException = alreadyExistsException;
  }

  public void modify(T object, Function<T, PermissionCheck> permissionCheck, AroundHandler<T, E> beforeUpdate, AroundHandler<T, E> afterUpdate) throws E {
    T notModified = dao.get(object.getId());
    if (notModified != null) {
      permissionCheck.apply(notModified).check();
      AssertUtil.assertIsValid(object);

      beforeUpdate.handle(notModified);

      object.setLastModified(System.currentTimeMillis());
      object.setCreationDate(notModified.getCreationDate());

      dao.modify(object);

      afterUpdate.handle(notModified);
    } else {
      throw notFoundException.apply(object);
    }
  }

  public T create(T newObject, Supplier<PermissionCheck> permissionCheck, AroundHandler<T, E> beforeCreate, AroundHandler<T, E> afterCreate) throws E {
    return create(newObject, permissionCheck, beforeCreate, afterCreate, dao::contains);
  }

  public T create(T newObject, Supplier<PermissionCheck> permissionCheck, AroundHandler<T, E> beforeCreate, AroundHandler<T, E> afterCreate, Predicate<T> existsCheck) throws E {
    permissionCheck.get().check();
    AssertUtil.assertIsValid(newObject);
    if (existsCheck.test(newObject)) {
      throw alreadyExistsException.apply(newObject);
    }
    newObject.setCreationDate(System.currentTimeMillis());
    beforeCreate.handle(newObject);
    dao.add(newObject);
    afterCreate.handle(newObject);
    return newObject;
  }

  public void delete(T toDelete, Supplier<PermissionCheck> permissionCheck, AroundHandler<T, E> beforeDelete, AroundHandler<T, E> afterDelete) throws E {
    permissionCheck.get().check();
    if (dao.contains(toDelete)) {
      beforeDelete.handle(toDelete);
      dao.delete(toDelete);
      afterDelete.handle(toDelete);
    } else {
      throw notFoundException.apply(toDelete);
    }
  }

  @FunctionalInterface
  public interface AroundHandler<T extends ModelObject, E extends Exception> {
    void handle(T notModified) throws E;
  }
}
