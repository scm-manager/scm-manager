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

import React from "react";
import { headingToAnchorId } from "./MarkdownHeadingRenderer";

describe("headingToAnchorId tests", () => {
  it("should lower case the text", () => {
    expect(headingToAnchorId("Hello")).toBe("hello");
    expect(headingToAnchorId("HeLlO")).toBe("hello");
    expect(headingToAnchorId("HELLO")).toBe("hello");
  });

  it("should replace spaces with hyphen", () => {
    expect(headingToAnchorId("awesome stuff")).toBe("awesome-stuff");
    expect(headingToAnchorId("a b c d e f")).toBe("a-b-c-d-e-f");
  });
});
