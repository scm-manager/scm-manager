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

import { calculateBarLength } from "./aheadBehind";

describe("ahead/behind percentage", () => {
  it("0 should have percentage value of 5", () => {
    const percentage = calculateBarLength(0);
    expect(percentage).toEqual(5);
  });

  let lastPercentage = 5;
  for (let changesets = 1; changesets < 4000; changesets++) {
    it(`${changesets} should have percentage value less or equal to last value`, () => {
      const percentage = calculateBarLength(changesets);
      expect(percentage).toBeGreaterThanOrEqual(lastPercentage);
      lastPercentage = percentage;
    });
  }

  it("10000 should not have percentage value bigger than 100", () => {
    const percentage = calculateBarLength(10000);
    expect(percentage).toEqual(100);
  });
});
