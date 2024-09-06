/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
