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

export const formatPublicKey = (key: string) => {
  const parts = key.split(/\s+/);
  if (parts.length === 3) {
    return parts[0] + " ... " + parts[2];
  } else if (parts.length === 2) {
    if (parts[0].length >= parts[1].length) {
      return parts[0].substring(0, 7) + "... " + parts[1];
    } else {
      const keyLength = parts[1].length;
      return parts[0] + " ..." + parts[1].substring(keyLength - 7);
    }
  } else {
    const keyLength = parts[0].length;
    if (keyLength < 15) {
      return parts[0];
    }
    return parts[0].substring(0, 7) + "..." + parts[0].substring(keyLength - 7);
  }
};
