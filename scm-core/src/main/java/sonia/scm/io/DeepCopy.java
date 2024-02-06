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
