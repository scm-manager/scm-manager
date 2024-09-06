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

import { validation } from "@scm-manager/ui-components";

const { isNameValid, isMailValid, isPathValid } = validation;

export { isNameValid, isMailValid, isPathValid };

export const isDisplayNameValid = (displayName: string) => {
  if (displayName) {
    return true;
  }
  return false;
};
export const isPasswordValid = (password: string) => {
  return password.length >= 6 && password.length < 1024;
};
