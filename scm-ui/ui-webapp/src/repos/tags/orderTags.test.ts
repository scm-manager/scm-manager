/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import orderTags from "./orderTags";

const tag1 = {
  name: "tag1",
  revision: "revision1",
  date: new Date(2020, 1, 1),
  _links: {}
};
const tag2 = {
  name: "tag2",
  revision: "revision2",
  date: new Date(2020, 1, 3),
  _links: {}
};
const tag3 = {
  name: "tag3",
  revision: "revision3",
  date: new Date(2020, 1, 2),
  _links: {}
};

describe("order tags", () => {
  it("should order tags descending by date", () => {
    const tags = [tag1, tag2, tag3];
    orderTags(tags);
    expect(tags).toEqual([tag2, tag3, tag1]);
  });
  it("should order tags ascending by name", () => {
    const tags = [tag1, tag2, tag3];
    orderTags(tags, "name_asc");
    expect(tags).toEqual([tag1, tag2, tag3]);
  });
  it("should order tags descending by name", () => {
    const tags = [tag1, tag2, tag3];
    orderTags(tags, "name_desc");
    expect(tags).toEqual([tag3, tag2, tag1]);
  });
});
