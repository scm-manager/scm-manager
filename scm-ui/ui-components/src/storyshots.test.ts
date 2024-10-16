/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
      querySelector: () => {},
    };
  }
};

async function wait(delay: number) {
  return act(
    () =>
      new Promise((resolve) => {
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
  createNodeMock,
});

initStoryshots({
  asyncJest: true,
  framework: "react",
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
  },
});
