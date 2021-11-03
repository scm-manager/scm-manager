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
