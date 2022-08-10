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
import React, { FC, useEffect, useMemo, useRef, useState } from "react";
import mermaid from "mermaid";
import ErrorNotification from "../ErrorNotification";
import Loading from "../Loading";

const MermaidCodeRenderer: FC<extensionPoints.MarkdownCodeRenderer["props"]> = ({ value }) => {
  const [content, setContent] = useState<string>("Loading...");
  const [error, setError] = useState<Error>();
  const id = useRef(`MERMAID-${Date.now()}`);
  const dangerousHtmlObject = useMemo(() => ({ __html: content }), [content]);

  useEffect(() => {
    try {
      setError(undefined);
      setContent("");
      mermaid.render(id.current, value, (svgCode) => {
        setContent(svgCode);
      });
    } catch (e) {
      if (e instanceof Error) {
        setError(e);
      } else {
        // @ts-ignore Unknown mermaid error
        setError(new Error(e.str));
      }
    }
  }, [value]);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (content) {
    return <figure className="mermaid" dangerouslySetInnerHTML={dangerousHtmlObject} />;
  }

  return <Loading />;
};

export default MermaidCodeRenderer;
