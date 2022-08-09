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

import { extensionPoints } from "@scm-manager/ui-extensions";
import React, { FC, useEffect, useRef, useState } from "react";
import mermaid from "mermaid";
import Loading from "../Loading";

const MermaidCodeRenderer: FC<extensionPoints.MarkdownCodeRenderer["props"]> = ({ value }) => {
  const [content, setContent] = useState<string>();
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    try {
      mermaid.render("MERMAID", value, (svgCode, bindFunctions) => {
        setContent(svgCode);
        if (ref.current) {
          bindFunctions(ref.current);
        }
      });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
      // @ts-ignore mermaid error
      setContent(e.str);
    }
  }, [value]);

  if (content) {
    return <div ref={ref} className="mermaid" dangerouslySetInnerHTML={{ __html: content }} />;
  }

  return <Loading />;
};

export default MermaidCodeRenderer;
