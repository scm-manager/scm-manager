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

export const isDevBuild = () =>
  ((window as unknown as { scmStage: string }).scmStage || "").toUpperCase() !== "PRODUCTION";

export const createAttributesForTesting = (testId?: string) => {
  if (!testId) {
    return undefined;
  }
  return {
    "data-testid": normalizeTestId(testId),
  };
};

// keep this weird function instead of replaceAll because of browser compatibility
const normalizeTestId = (testId?: string) => {
  let id = testId?.toLowerCase();
  while (id?.includes(" ")) {
    id = id.replace(" ", "-");
  }
  return id;
};
