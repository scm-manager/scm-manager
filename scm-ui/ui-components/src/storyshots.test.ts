import path from "path";
import initStoryshots, { snapshotWithOptions } from "@storybook/addon-storyshots";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const createNodeMock = (element: any) => {
  if (element.type === "tr") {
    return {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      querySelector: (selector: string) => {}
    };
  }
  return null;
};

initStoryshots({
  configPath: path.resolve(__dirname, "..", ".storybook"),
  // fix snapshot tests with react-diff-view which uses a ref on tr
  // @see https://github.com/storybookjs/storybook/pull/1090
  test: snapshotWithOptions({
    createNodeMock
  })
});
