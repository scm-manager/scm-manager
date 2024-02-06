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
    
package sonia.scm.security;

import sonia.scm.plugin.ExtensionPoint;

/**
 * AccessTokenEnricher is able to enhance the {@link AccessToken}, before it is delivered to the client. 
 * AccessTokenEnricher can be used to add custom fields to the {@link AccessToken}. The enricher is always called before
 * an {@link AccessToken} is build by the {@link AccessTokenBuilder}.
 * 
 * @since 2.0.0
 */
@ExtensionPoint
public interface AccessTokenEnricher {
  
  /**
   * Enriches the access token by adding fields to the {@link AccessTokenBuilder}.
   * 
   * @param builder access token builder
   */
  void enrich(AccessTokenBuilder builder);
}
