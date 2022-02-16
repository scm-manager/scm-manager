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
import { act, create, ReactTestRenderer } from "react-test-renderer";
import { StoryContext } from "@storybook/react";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const createNodeMock = (element: any) => {
  if (element.type === "tr") {
    return {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      querySelector: () => {}
    };
  }
};

async function wait(delay: number) {
  return act(
    () =>
      new Promise(resolve => {
        setTimeout(resolve, delay);
      })
  );
}

async function runAsyncTest(story: StoryContext) {
  const storyElement = story.render();
  let renderer: ReactTestRenderer | undefined;
  act(() => {
    renderer = create(storyElement);
  });

  // For Flow's benefit
  if (!renderer) {
    return;
  }

  // Let one render cycle pass before rendering snapshot
  await wait(0);
  expect(renderer).toMatchSnapshot();

  renderer.unmount();
}

const syncTest = snapshotWithOptions({
  // @ts-ignore types seems not to match
  createNodeMock
});

initStoryshots({
  asyncJest: true,
  configPath: path.resolve(__dirname, "..", ".storybook"),
  test: ({ story, context, done, ...rest }) => {
    if (story.parameters?.storyshots?.async) {
      runAsyncTest(story).then(done);
    } else {
      syncTest({ story, context, ...rest });
      if (done) {
        done();
      }
    }
  }
});
