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
import textSplitAndReplace from "./textSplitAndReplace";

export type Replacement = {
  textToReplace: string;
  replacement: ReactNode;
  replaceAll?: boolean;
};

type Props = {
  text: string;
  replacements: Replacement[];
  textWrapper?: (s: string) => ReactNode;
};

const defaultTextWrapper = (s: string) => {
  const first = s.startsWith(" ") ? <>&nbsp;</> : "";
  const last = s.endsWith(" ") ? <>&nbsp;</> : "";
  return (
    <>
      {first}
      {s}
      {last}
    </>
  );
};

const SplitAndReplace: FC<Props> = ({ text, replacements, textWrapper = defaultTextWrapper }) => {
  const parts = textSplitAndReplace<ReactNode>(text, replacements, textWrapper);
  if (parts.length === 0) {
    return <>{parts[0]}</>;
  }
  return (
    <>
      {parts.map((part, index) => (
        <React.Fragment key={index}>{part}</React.Fragment>
      ))}
    </>
  );
};

export default SplitAndReplace;
