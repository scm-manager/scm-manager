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


package sonia.scm.plugin.ext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.annotation.Annotation;

/**
 * The annotation scanner is able to scan archives and directories for
 * annotated classes. Each annotated class can be processed with a
 * {@link AnnotationProcessor}.
 *
 * @author Sebastian Sdorra
 * @since 1.26
 */
public interface AnnotationScanner
{

  /**
   * Adds a {@link AnnotationProcessor} for the given annotation.
   *
   *
   * @param annotationClass class of the annotation
   * @param processor processor
   * @param <T> annotation type
   */
  public <T extends Annotation> void addProcessor(Class<T> annotationClass,
    AnnotationProcessor<T> processor);

  /**
   * Scans the given archive for annotations.
   *
   *
   * @param archive archive input stream
   *
   * @throws IOException
   */
  public void scanArchive(InputStream archive) throws IOException;

  /**
   * Scans a directory for annotated classes.
   *
   *
   * @param directory directory to scan
   *
   * @throws IOException
   */
  public void scanDirectory(File directory) throws IOException;
}
