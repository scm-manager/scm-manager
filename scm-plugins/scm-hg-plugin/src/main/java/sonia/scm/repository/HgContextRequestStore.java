/**
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
package sonia.scm.repository;

import com.google.inject.servlet.RequestScoped;

/**
 * Holds an instance of {@link HgContext} in the request scope.
 *
 * <p>The problem seems to be that guice had multiple options for injecting HgContext. {@link HgContextProvider}
 * bound via Module and {@link HgContext} bound void {@link RequestScoped} annotation. It looks like that Guice 4
 * injects randomly the one or the other, in SCMv1 (Guice 3) everything works as expected.</p>
 *
 * <p>To fix the problem we have created this class annotated with {@link RequestScoped}, which holds an instance
 * of {@link HgContext}. This way only the {@link HgContextProvider} is used for injection.</p>
 */
@RequestScoped
public class HgContextRequestStore {

  private final HgContext context = new HgContext();

  public HgContext get() {
    return context;
  }

}
