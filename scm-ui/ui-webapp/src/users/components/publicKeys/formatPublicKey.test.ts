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

describe("format public key tests", () => {
  it("should format the given key", () => {
    const key = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n\nA1B2C3D4E5F6HDJSURNSKFHSNEKFHK443MKD\n-----END PGP PUBLIC KEY BLOCK-----";
    expect(formatPublicKey(key)).toEqual("A1B2C3D4E5F6HDJ");
  });

  it("should format bad formatted key", () => {
    const key = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n\n\nA1B2C3D4E5F6HDJSURNSKFHSNEKFHK443MKD\n\n\n-----END PGP PUBLIC KEY BLOCK-----";
    expect(formatPublicKey(key)).toEqual("-----BEGIN PGP ");
  });
});
