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
import { formatPublicKey } from "./formatPublicKey";

describe("format authorized key tests", () => {
  it("should format the given key", () => {
    const key = "ssh-rsa ACB0DEFGHIJKLMOPQRSTUVWXYZ tricia@hitchhiker.com";
    expect(formatPublicKey(key)).toEqual("ssh-rsa ... tricia@hitchhiker.com");
  });

  it("should use the first chars of the key without prefix", () => {
    const key = "ACB0DEFGHIJKLMOPQRSTUVWXYZ tricia@hitchhiker.com";
    expect(formatPublicKey(key)).toEqual("ACB0DEF... tricia@hitchhiker.com");
  });

  it("should use the last chars of the key without suffix", () => {
    const key = "ssh-rsa ACB0DEFGHIJKLMOPQRSTUVWXYZ";
    expect(formatPublicKey(key)).toEqual("ssh-rsa ...TUVWXYZ");
  });

  it("should use a few chars from the beginning and a few from the end, if the key has no prefix and suffix", () => {
    const key = "ACB0DEFGHIJKLMOPQRSTUVWXYZ0123456789";
    expect(formatPublicKey(key)).toEqual("ACB0DEF...3456789");
  });

  it("should return the whole string for a short key", () => {
    const key = "ABCDE";
    expect(formatPublicKey(key)).toEqual("ABCDE");
  });
});
