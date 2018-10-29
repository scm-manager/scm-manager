//@flow
import fetchMock from "fetch-mock";
import {
  getContent,
  getLanguage,
  getProgrammingLanguage
} from "./SourcecodeViewer";

describe("get content", () => {
  const CONTENT_URL = "/repositories/scmadmin/TestRepo/content/testContent";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should return content", done => {
    fetchMock.getOnce("/api/v2" + CONTENT_URL, "This is a testContent");

    getContent(CONTENT_URL).then(content => {
      expect(content).toBe("This is a testContent");
      done();
    });
  });

  it("should return language", done => {
    let headers = {
      "X-Programming-Language": "JAVA"
    };

    fetchMock.head("/api/v2" + CONTENT_URL, {
      headers
    });

    getProgrammingLanguage(CONTENT_URL).then(content => {
      expect(content.language).toBe("JAVA");
      done();
    });
  });
});

describe("get correct language type", () => {
  it("should return javascript", () => {
    expect(getLanguage("JAVASCRIPT")).toBe("javascript");
  });
  it("should return nothing for plain text", () => {
    expect(getLanguage("")).toBe("");
  });
});
