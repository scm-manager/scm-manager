import { createUrl, isDiffSupported } from "./ChangesetDiff";

describe("isDiffSupported tests", () => {
  it("should return true if diff link is defined", () => {
    const supported = isDiffSupported({
      _links: {
        diff: {
          href: "http://diff"
        }
      }
    });

    expect(supported).toBe(true);
  });

  it("should return true if parsed diff link is defined", () => {
    const supported = isDiffSupported({
      _links: {
        diffParsed: {
          href: "http://diff"
        }
      }
    });

    expect(supported).toBe(true);
  });

  it("should return false if not diff link was provided", () => {
    const supported = isDiffSupported({
      _links: {}
    });

    expect(supported).toBe(false);
  });
});

describe("createUrl tests", () => {
  it("should return the diff url, if only diff url is defined", () => {
    const url = createUrl({
      _links: {
        diff: {
          href: "http://diff"
        }
      }
    });

    expect(url).toBe("http://diff?format=GIT");
  });

  it("should return the diff parsed url, if only diff parsed url is defined", () => {
    const url = createUrl({
      _links: {
        diffParsed: {
          href: "http://diff-parsed"
        }
      }
    });

    expect(url).toBe("http://diff-parsed");
  });

  it("should return the diff parsed url, if both diff links are defined", () => {
    const url = createUrl({
      _links: {
        diff: {
          href: "http://diff"
        },
        diffParsed: {
          href: "http://diff-parsed"
        }
      }
    });

    expect(url).toBe("http://diff-parsed");
  });

  it("should throw an error if no diff link is defined", () => {
    expect(() =>
      createUrl({
        _links: {}
      })
    ).toThrow();
  });
});
