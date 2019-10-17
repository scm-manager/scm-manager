//@flow
import fetchMock from "fetch-mock";
import { getHistory } from "./history";

describe("get content type", () => {
  const FILE_URL = "/repositories/scmadmin/TestRepo/history/file";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  const history = {
    page: 0,
    pageTotal: 10,
    _links: {
      self: {
        href: "/repositories/scmadmin/TestRepo/history/file?page=0&pageSize=10"
      },
      first: {
        href: "/repositories/scmadmin/TestRepo/history/file?page=0&pageSize=10"
      },
      next: {
        href: "/repositories/scmadmin/TestRepo/history/file?page=1&pageSize=10"
      },
      last: {
        href: "/repositories/scmadmin/TestRepo/history/file?page=9&pageSize=10"
      }
    },
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
  };

  it("should return history", done => {
    fetchMock.get("/api/v2" + FILE_URL, history);

    getHistory(FILE_URL).then(content => {
      expect(content.changesets).toEqual(history._embedded.changesets);
      expect(content.pageCollection.page).toEqual(history.page);
      expect(content.pageCollection.pageTotal).toEqual(history.pageTotal);
      expect(content.pageCollection._links).toEqual(history._links);
      done();
    });
  });
});
