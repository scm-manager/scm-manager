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

import path from "path";
import initStoryshots, { snapshotWithOptions } from "@storybook/addon-storyshots";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const createNodeMock = (element: any) => {
  if (element.type === "tr") {
    return {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      querySelector: (selector: string) => {},
    };
  }
};

initStoryshots({
  configPath: path.resolve(__dirname, "..", ".storybook"),
  // fix snapshot tests with react-diff-view which uses a ref on tr
  // @see https://github.com/storybookjs/storybook/pull/1090
  test: snapshotWithOptions({
    // @ts-ignore types seems not to match
    createNodeMock,
  }),
});
