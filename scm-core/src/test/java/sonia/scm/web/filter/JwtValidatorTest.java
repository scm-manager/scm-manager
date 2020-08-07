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

package sonia.scm.web.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.web.filter.JwtValidator.isJwtTokenExpired;

class JwtValidatorTest {

  @Test
  void shouldReturnFalseIfNotJwtToken() {
    String raw = "scmadmin.scmadmin.scmadmin";

    boolean result = isJwtTokenExpired(raw);

    assertThat(result).isFalse();
  }

  @Test
  void shouldValidateExpiredJwtToken() {
    String raw = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzY21hZG1pbiIsImp0aSI6IjNqUzZ4TzMwUzEiLCJpYXQiOjE1OTY3ODA5Mjgs"
      + "ImV4cCI6MTU5Njc0NDUyOCwic2NtLW1hbmFnZXIucmVmcmVzaEV4cGlyYXRpb24iOjE1OTY4MjQxMjg2MDIsInNjbS1tYW5hZ2VyLnB"
      + "hcmVudFRva2VuSWQiOiIzalM2eE8zMFMxIn0.utZLmzGZr-M6MP19yrd0dgLPkJ0u1xojwHKQi36_QAs";

    boolean result = isJwtTokenExpired(raw);

    assertThat(result).isTrue();
  }

  @Test
  void shouldValidateNotExpiredJwtToken() {
    String raw = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzY21hZG1pbiIsImp0aSI6IjNqUzZ4TzMwUzEiLCJpYXQiOjE1OTY3ODA5Mjgs"
      + "ImV4cCI6NTU5Njc0NDUyOCwic2NtLW1hbmFnZXIucmVmcmVzaEV4cGlyYXRpb24iOjE1OTY4MjQxMjg2MDIsInNjbS1tYW5hZ2VyLnB"
      + "hcmVudFRva2VuSWQiOiIzalM2eE8zMFMxIn0.cvK4E58734T2PqtEqqhYCInnX_uryUkMhRNX-94riY0";

    boolean result = isJwtTokenExpired(raw);

    assertThat(result).isFalse();
  }

}
