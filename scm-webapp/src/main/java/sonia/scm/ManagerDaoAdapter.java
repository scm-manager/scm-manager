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

package sonia.scm;

import com.github.sdorra.ssp.PermissionCheck;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.auditlog.Auditor;
import sonia.scm.auditlog.EntryCreationContext;
import sonia.scm.util.AssertUtil;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManagerDaoAdapter<T extends ModelObject> {

  private final GenericDAO<T> dao;
  private final Set<Auditor> auditors;

  public ManagerDaoAdapter(GenericDAO<T> dao, Set<Auditor> auditors) {
    this.dao = dao;
    this.auditors = auditors;
  }

  public void modify(T object, Function<T, PermissionCheck> permissionCheck, AroundHandler<T> beforeUpdate, AroundHandler<T> afterUpdate) {
    T notModified = dao.get(object.getId());
    if (notModified != null) {
      permissionCheck.apply(notModified).check();

      AssertUtil.assertIsValid(object);

      beforeUpdate.handle(notModified);

      object.setLastModified(System.currentTimeMillis());
      object.setCreationDate(notModified.getCreationDate());

      callAuditors(notModified, object);

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
    callAuditors(null, newObject);
    dao.add(newObject);
    afterCreate.handle(newObject);
    return newObject;
  }

  public void delete(T toDelete, Supplier<PermissionCheck> permissionCheck, AroundHandler<T> beforeDelete, AroundHandler<T> afterDelete) {
    permissionCheck.get().check();
    if (dao.contains(toDelete)) {
      beforeDelete.handle(toDelete);
      callAuditors(toDelete, null);
      dao.delete(toDelete);
      afterDelete.handle(toDelete);
    } else {
      throw new NotFoundException(toDelete.getClass(), toDelete.getId());
    }
  }

  private void callAuditors(T notModified, T newObject) {
    if ((newObject == null? notModified: newObject).getClass().isAnnotationPresent(AuditEntry.class)) {
      auditors.forEach(s -> s.createEntry(new EntryCreationContext<>(newObject, notModified)));
    }
  }

  @FunctionalInterface
  public interface AroundHandler<T extends ModelObject> {
    void handle(T notModified);
  }
}
