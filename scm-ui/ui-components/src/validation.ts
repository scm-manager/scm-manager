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

export const nameRegex = /^(?:(?:[^:/?#;&=\s@%\\][^:/?#;&=%\\]*[^:/?#;&=\s%\\]+)|[^:/?#;&=\s@%\\])$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

export const branchRegex = /^[\w-,;\]{}@&+=$#`|<>]([\w-,;\]{}@&+=$#`|<>/.]*[\w-,;\]{}@&+=$#`|<>])?$/;

export const isBranchValid = (name: string) => {
  return branchRegex.test(name);
};

const mailRegex = /^[ -~]+@[A-Za-z0-9][\w\-.]*\.[A-Za-z0-9][A-Za-z0-9-]+$/;

export const isMailValid = (mail: string) => {
  return mailRegex.test(mail);
};

export const isNumberValid = (number: number) => {
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
