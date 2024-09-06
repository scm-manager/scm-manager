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

const isUndefined = (o: any, key: string, nested?: string) => {
  if (typeof o === "undefined" || typeof o[key] === "undefined") {
    return true;
  }
  if (nested) {
    return typeof o[key][nested] === "undefined";
  }
  return false;
};

export const byKey = (key: string) => {
  return (a: any, b: any) => {
    if (isUndefined(a, key)) {
      return 1;
    }

    if (isUndefined(b, key)) {
      return -1;
    }

    if (a[key] < b[key]) {
      return -1;
    } else if (a[key] > b[key]) {
      return 1;
    } else {
      return 0;
    }
  };
};

export const byValueLength = (key: string) => {
  return (a: any, b: any) => {
    if (isUndefined(a, key)) {
      return 1;
    }

    if (isUndefined(b, key)) {
      return -1;
    }

    if (a[key].length < b[key].length) {
      return -1;
    } else if (a[key].length > b[key].length) {
      return 1;
    } else {
      return 0;
    }
  };
};

export const byNestedKeys = (key: string, nestedKey: string) => {
  return (a: any, b: any) => {
    if (isUndefined(a, key, nestedKey)) {
      return 1;
    }

    if (isUndefined(b, key, nestedKey)) {
      return -1;
    }

    if (a[key][nestedKey] < b[key][nestedKey]) {
      return -1;
    } else if (a[key][nestedKey] > b[key][nestedKey]) {
      return 1;
    } else {
      return 0;
    }
  };
};

export default {
  byKey,
  byValueLength,
  byNestedKeys,
};
