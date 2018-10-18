// @flow

import { parseDescription } from "./changesets";

describe("parseDescription tests", () => {
  it("should return a description with title and message", () => {
    const desc = parseDescription("Hello\nTrillian");
    expect(desc.title).toBe("Hello");
    expect(desc.message).toBe("Trillian");
  });

  it("should return a description with title and without message", () => {
    const desc = parseDescription("Hello Trillian");
    expect(desc.title).toBe("Hello Trillian");
  });
});
