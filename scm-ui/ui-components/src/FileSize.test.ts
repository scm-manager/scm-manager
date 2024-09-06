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

import FileSize from "./FileSize";

it("should format bytes", () => {
  expect(FileSize.format(0)).toBe("0 B");
  expect(FileSize.format(160)).toBe("160 B");
  expect(FileSize.format(6304)).toBe("6.30 K");
  expect(FileSize.format(28792588)).toBe("28.79 M");
  expect(FileSize.format(1369510189)).toBe("1.37 G");
  expect(FileSize.format(42949672960)).toBe("42.95 G");
});
