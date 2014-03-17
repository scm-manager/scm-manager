/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * {@link ObjectInputStream} implementation which uses the context class loader 
 * to resolve classes.
 *
 * @author Sebastian Sdorra
 * @since 1.36
 */
public class ScmObjectInputStream extends ObjectInputStream
{

  /**
   * the logger for ScmObjectInputStream
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmObjectInputStream.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public ScmObjectInputStream(InputStream stream) throws IOException
  {
    super(stream);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc)
    throws IOException, ClassNotFoundException
  {
    Class<?> clazz = null;
    ClassLoader classLoader = getClassLoader();

    try
    {
      clazz = classLoader.loadClass(desc.getName());
    }
    catch (ClassNotFoundException ex)
    {
      // do not log the exception, because the class 
      // is mostly found by the parent method.
    }

    if (clazz == null)
    {
      clazz = super.resolveClass(desc);
    }

    return clazz;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the context class loader if available. If the context class loader
   * is not available the method will fall back to the class loader which has
   * load this class.
   *
   *
   * @return context class loader or default class loader
   */
  private ClassLoader getClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      logger.debug("could not find context class loader, fall back to default");
      classLoader = ScmObjectInputStream.class.getClassLoader();
    }

    return classLoader;
  }
}
