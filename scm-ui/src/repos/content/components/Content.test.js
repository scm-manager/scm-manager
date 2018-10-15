//@flow
import fetchMock from "fetch-mock";
import { getContentType } from "./Content";

describe("get content type", () => {
  const CONTENT_URL = "/repositories/scmadmin/TestRepo/content/testContent";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  xit("should return content", done => {
    fetchMock.head("/api/v2" + CONTENT_URL, {
      "Content-Type": "text/plain"
    });

    getContentType(CONTENT_URL).then(content => {
      expect(content).toBe("This is a testContent");
      done();
    });
  });
});
