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

package sonia.scm.io;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * {@link ObjectInputStream} implementation which uses the context class loader
 * to resolve classes.
 *
 * @since 1.36
 */
public class ScmObjectInputStream extends ObjectInputStream {

  private static final Logger logger =
    LoggerFactory.getLogger(ScmObjectInputStream.class);


  public ScmObjectInputStream(InputStream stream) throws IOException {
    super(stream);
  }


 
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc)
    throws IOException, ClassNotFoundException {
    Class<?> clazz = null;
    ClassLoader classLoader = getClassLoader();

    try {
      clazz = classLoader.loadClass(desc.getName());
    } catch (ClassNotFoundException ex) {
      // do not log the exception, because the class 
      // is mostly found by the parent method.
    }

    if (clazz == null) {
      clazz = super.resolveClass(desc);
    }

    return clazz;
  }

  private ClassLoader getClassLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null) {
      logger.debug("could not find context class loader, fall back to default");
      classLoader = ScmObjectInputStream.class.getClassLoader();
    }

    return classLoader;
  }
}
