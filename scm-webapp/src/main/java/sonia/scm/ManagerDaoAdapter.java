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
    
package sonia.scm;

import com.github.sdorra.ssp.PermissionCheck;
import sonia.scm.util.AssertUtil;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManagerDaoAdapter<T extends ModelObject> {

  private final GenericDAO<T> dao;

  public ManagerDaoAdapter(GenericDAO<T> dao) {
    this.dao = dao;
  }

  public void modify(T object, Function<T, PermissionCheck> permissionCheck, AroundHandler<T> beforeUpdate, AroundHandler<T> afterUpdate) {
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
      throw new NotFoundException(object.getClass(), object.getId());
    }
  }

  public T create(T newObject, Supplier<PermissionCheck> permissionCheck, AroundHandler<T> beforeCreate, AroundHandler<T> afterCreate) {
    return create(newObject, permissionCheck, beforeCreate, afterCreate, o -> {
      if (dao.contains(o)) {
        throw new AlreadyExistsException(newObject);
      }
    });
  }

  public T create(T newObject, Supplier<PermissionCheck> permissionCheck, AroundHandler<T> beforeCreate, AroundHandler<T> afterCreate, Consumer<T> existsCheck) {
    permissionCheck.get().check();
    AssertUtil.assertIsValid(newObject);
    existsCheck.accept(newObject);
    newObject.setCreationDate(System.currentTimeMillis());
    beforeCreate.handle(newObject);
    dao.add(newObject);
    afterCreate.handle(newObject);
    return newObject;
  }

  public void delete(T toDelete, Supplier<PermissionCheck> permissionCheck, AroundHandler<T> beforeDelete, AroundHandler<T> afterDelete) {
    permissionCheck.get().check();
    if (dao.contains(toDelete)) {
      beforeDelete.handle(toDelete);
      dao.delete(toDelete);
      afterDelete.handle(toDelete);
    } else {
      throw new NotFoundException(toDelete.getClass(), toDelete.getId());
    }
  }

  @FunctionalInterface
  public interface AroundHandler<T extends ModelObject> {
    void handle(T notModified);
  }
}
