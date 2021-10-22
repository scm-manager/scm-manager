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

import React, { FC } from "react";

import { File, Link } from "@scm-manager/ui-types";
import Notification from "./Notification";

type Props = {
  src: string | File;
  height?: string;
};

const createHref = (src: string | File): string => {
  if (typeof src === "string") {
    return src;
  }
  return (src._links.self as Link).href;
};

const PdfViewer: FC<Props> = ({ src, height = "50rem" }) => {
  const href = createHref(src);
  return (
    <div style={{ height }}>
      <object height="100%" width="100%" type="application/pdf" data={href + "#toolbar=0&navpanes=0&scrollbar=0"}>
        <Notification type="warning">
          Failed to display the pdf. Please download it from <a href={href}>here</a>.
        </Notification>
      </object>
    </div>
  );
};

export default PdfViewer;
