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

package sonia.scm.api.v2.resources;

import com.google.inject.ProvisionException;

public class OutOfRequestScopeException extends RuntimeException {

  public OutOfRequestScopeException(ProvisionException cause) {
    super("Out of request scope\n\n" +
        "==============================================================\n" +
        "Cannot create links, because we are out of the request scope.\n" +
        "Most probably this is because you were trying to create a link\n" +
        "in a context that has no encapsulating http request. It is not\n" +
        "possible to create these links outside of requests, because\n" +
        "the computation uses the originating url of the http request\n" +
        "as a basis. To create urls outside of request scopes, please\n" +
        "inject an instance of ScmConfiguration and use getBaseUrl().\n" +
        "==============================================================\n",
      cause);
  }
}
