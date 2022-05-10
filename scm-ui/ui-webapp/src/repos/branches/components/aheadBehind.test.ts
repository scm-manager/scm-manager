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

import { calculateBarLength } from "./aheadBehind";

describe("ahead/behind percentage", () => {
  it("0 should have percentage value of 5", () => {
    const percentage = calculateBarLength(0);
    expect(percentage).toEqual(5);
  });

  let lastPercentage = 5;
  for (let changesets = 1; changesets < 4000; changesets++) {
    // eslint-disable-next-line no-loop-func
    it(`${changesets} should have percentage value less or equal to last value`, () => {
      const percentage = calculateBarLength(changesets);
      expect(percentage).toBeGreaterThanOrEqual(lastPercentage);
      lastPercentage = percentage;
    });
  }

  it("10000 should not have percentage value bigger than 100", () => {
    const percentage = calculateBarLength(10000);
    expect(percentage).toEqual(100);
  });
});
