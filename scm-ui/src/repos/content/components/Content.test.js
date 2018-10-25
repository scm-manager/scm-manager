//@flow
import fetchMock from "fetch-mock";
import { getContentType } from "./Content";

describe("get content type", () => {
  const CONTENT_URL = "/repositories/scmadmin/TestRepo/content/testContent";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should return content", done => {
    let headers = {
      "Content-Type": "application/text"
    };

    fetchMock.head("/api/v2" + CONTENT_URL, {
      headers
    });

    getContentType(CONTENT_URL).then(content => {
      expect(content).toBe("application/text");
      done();
    });
  });
});
