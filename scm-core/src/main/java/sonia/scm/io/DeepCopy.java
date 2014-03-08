/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closer;

//~--- JDK imports ------------------------------------------------------------

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
 * @author Sebastian Sdorra
 * @since 1.29
 * @see <a href="http://javatechniques.com/blog/faster-deep-copies-of-java-objects" target="_blank">http://javatechniques.com/blog/faster-deep-copies-of-java-objects</a>
 */
public final class DeepCopy
{

  /**
   * Returns a copy of the object, or null if the object cannot
   * be serialized.
   *
   * @param orig
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   */
  public static <T> T copy(T orig) throws IOException
  {
    T obj = null;

    Closer closer = Closer.create();

    try
    {

      // Write the object out to a byte array
      FastByteArrayOutputStream fbos =
        closer.register(new FastByteArrayOutputStream());
      ObjectOutputStream out = closer.register(new ObjectOutputStream(fbos));

      out.writeObject(orig);
      out.flush();
      out.close();

      // Retrieve an input stream from the byte array and read
      // a copy of the object back in.
      // use ScmObjectInputStream to solve ClassNotFoundException
      // for plugin classes
      ObjectInputStream in =
        closer.register(new ScmObjectInputStream(fbos.getInputStream()));

      obj = (T) in.readObject();
    }
    catch (Exception ex)
    {
      throw new IOException("could not copy object", ex);
    }
    finally
    {
      closer.close();
    }

    return obj;
  }
}
