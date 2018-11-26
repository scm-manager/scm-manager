//@flow
import fetchMock from "fetch-mock";
import { getHistory } from "./history";

describe("get content type", () => {
  const FILE_URL = "/repositories/scmadmin/TestRepo/history/file";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should return history", done => {
    let changesets: {
      changesets: [
        {
          id: "1234"
        },
        {
          id: "2345"
        }
      ]
    };
    let history = {
      _embedded: {
        changesets
      }
    };

    fetchMock.get("/api/v2" + FILE_URL, {
      history
    });

    getHistory(FILE_URL).then(content => {
      expect(content.changesets).toBe(changesets);
      done();
    });
  });
});
