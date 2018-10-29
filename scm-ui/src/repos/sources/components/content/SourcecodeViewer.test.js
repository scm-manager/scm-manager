//@flow
import fetchMock from "fetch-mock";
import { getContent, getLanguage } from "./SourcecodeViewer";

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
});

describe("get correct language Type", () => {
  it("should return javascript", () => {
    expect(getLanguage("application/javascript")).toBe("javascript");
  });
  it("should return text", () => {
    expect(getLanguage("text/plain")).toBe("plain");
  });
});
