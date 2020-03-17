///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

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
