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
    fetchMock.get("/api/v2" + FILE_URL, {
      page: 0,
      pageTotal: 1,
      _embedded: {
        changesets: [
          {
            id: "1234"
          },
          {
            id: "2345"
          }
        ]
      }
    });

    getHistory(FILE_URL).then(content => {
      expect(content.changesets).toEqual([
        {
          id: "1234"
        },
        {
          id: "2345"
        }
      ]);
      expect(content.pageCollection.page).toEqual(0);
      done();
    });
  });
});
