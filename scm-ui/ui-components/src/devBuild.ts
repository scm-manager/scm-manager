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

// eslint-disable-next-line @typescript-eslint/ban-ts-ignore
// @ts-ignore scmStage is set on the index page
export const isDevBuild = () => (window.scmStage || "").toUpperCase() !== "PRODUCTION";

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

export const createAttributesForTesting = (testId?: string) => {
  if (!testId) {
    return undefined;
  }
  return {
    "data-testid": normalizeTestId(testId),
  };
};

const normalizeTestId = (testId: string) => {
  let id = testId.toLowerCase();
  while (id.includes(" ")) {
    id = id.replace(" ", "-");
  }
  return id;
};
