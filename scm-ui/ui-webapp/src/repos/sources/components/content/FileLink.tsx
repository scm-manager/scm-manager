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
import React, { FC, ReactNode } from "react";
import { Link } from "react-router-dom";
import { File } from "@scm-manager/ui-types";

type Props = {
  baseUrl: string;
  file: File;
  children: ReactNode;
};

const createFolderLink = (base: string, file: File) => {
  let link = base;
  if (file.path) {
    let path = file.path;
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    link += "/" + path;
  }
  if (!link.endsWith("/")) {
    link += "/";
  }
  return link;
};

const createRelativeLink = (base: string, path: string) => {
  let link = base;
  link += path.split(".").join("");
  if (!link.endsWith("/")) {
    link += "/";
  }
  return link;
};

const FileLink: FC<Props> = ({ baseUrl, file, children }) => {
  if (file?.subRepository?.repositoryUrl) {
    const link = file.subRepository.repositoryUrl;
    if (link.startsWith("http://") || link.startsWith("https://")) {
      return <a href={link}>{children}</a>;
    } else if (link.startsWith(".")) {
      return <Link to={createRelativeLink(baseUrl, link)}>{children}</Link>;
    }
  }
  return <Link to={createFolderLink(baseUrl, file)}>{children}</Link>;
};

export default FileLink;
