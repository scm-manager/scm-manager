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


import com.google.common.io.Closer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is printed to System.err and
 * null is returned. Depending on your specific application, it might
 * make more sense to have copy(...) re-throw the exception.
 *
 * @since 1.29
 * @see <a href="http://javatechniques.com/blog/faster-deep-copies-of-java-objects" target="_blank">http://javatechniques.com/blog/faster-deep-copies-of-java-objects</a>
 */
public final class DeepCopy {

  private DeepCopy() {
  }
  
  /**
   * Returns a copy of the object, or null if the object cannot
   * be serialized.
   *
   * @param orig object to copy
   * @param <T> type of object to copy
   *
   * @return deep copy of object
   *
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public static <T> T copy(T orig) throws IOException {
    try (Closer closer = Closer.create()) {
      // Write the object out to a byte array
      FastByteArrayOutputStream fbos = closer.register(new FastByteArrayOutputStream());
      try (ObjectOutputStream out = new ObjectOutputStream(fbos)) {
        out.writeObject(orig);
        out.flush();
      }

      // Retrieve an input stream from the byte array and read
      // a copy of the object back in.
      // use ScmObjectInputStream to solve ClassNotFoundException
      // for plugin classes
      ObjectInputStream in = closer.register(new ScmObjectInputStream(fbos.getInputStream()));
      
      return (T) in.readObject();
    } catch (ClassNotFoundException ex) {
      throw new IOException("could not copy object", ex);
    }
  }
}
