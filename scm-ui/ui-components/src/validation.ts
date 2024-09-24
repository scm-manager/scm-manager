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

export const nameRegex = /^(?:(?:[^:/?#;&=\s@%\\][^:/?#;&=%\\]*[^:/?#;&=\s%\\]+)|[^:/?#;&=\s@%\\])$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

// See validation regex in Java class "Branch" for further details
export const branchRegex =
  /^[^.\\\s[~^:?*](?:[^\\\s[~^:?*]*[^.\\\s[~^:?*])?(?:\/[^.\\\s[~^:?*](?:[^\\\s[~^:?*]*[^.\\\s[~^:?*])?)*$/;

export const isBranchValid = (name: string) => {
  return branchRegex.test(name);
};

const mailRegex = /^[a-z0-9!#$%&'*+\/=?^_`{|}~" -]+(?:\.[a-z0-9!#$%&'*+\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/i;

export const isMailValid = (mail: string) => {
  return mailRegex.test(mail);
};

export const isNumberValid = (number: any) => {
  return !isNaN(number);
};

export const isPathValid = (path: string) => {
  return path !== "." && !path.includes("../") && !path.includes("//") && path !== "..";
};

const urlRegex = /^[A-Za-z0-9]+:\/\/[^\s$.?#].[^\s]*$/;

export const isUrlValid = (url: string) => {
  return urlRegex.test(url);
};

const filenameRegex = /^[^/\\:]+$/;

export const isFilenameValid = (filename: string) => {
  return filenameRegex.test(filename) && filename !== "." && filename !== ".." && !filename.includes("./");
};
