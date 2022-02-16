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

const PRE_TAG = "<|[[--";
const POST_TAG = "--]]|>";

type Props = {
  value: string;
};

const HighlightedFragment: FC<Props> = ({ value }) => {
  let content = value;

  const result = [];
  while (content.length > 0) {
    const start = content.indexOf(PRE_TAG);
    const end = content.indexOf(POST_TAG);
    if (start >= 0 && end > 0) {
      if (start > 0) {
        result.push(content.substring(0, start));
      }
      result.push(<mark>{content.substring(start + PRE_TAG.length, end)}</mark>);
      content = content.substring(end + POST_TAG.length);
    } else {
      result.push(content);
      break;
    }
  }
  return (
    <>
      {result.map((c, i) => (
        <React.Fragment key={i}>{c}</React.Fragment>
      ))}
    </>
  );
};

export default HighlightedFragment;
