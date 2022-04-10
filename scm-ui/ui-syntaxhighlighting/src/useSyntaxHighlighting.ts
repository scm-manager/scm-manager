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

import { useEffect, useRef, useState } from "react";

// @ts-ignore TODO
import theme from "./syntax-highlighting.module.css";
import { nanoid } from "nanoid";
import {
  HighlightingRequest,
  LoadThemeRequest,
  RefractorNode,
  Response,
  ResponseMessage,
  SuccessResponse,
} from "./types";

// WebWorker which creates tokens for syntax highlighting
// @ts-ignore
const worker = new Worker(
  // @ts-ignore TODO
  new URL("./SyntaxHighlighter.worker.ts", import.meta.url),
  {
    name: "SyntaxHighlighter",
    type: "module",
  }
);

worker.postMessage({ type: "theme", payload: theme } as LoadThemeRequest);

const isSuccess = (response: Response): response is SuccessResponse => {
  return response.type === "success";
};

export type UseSyntaxHighlightingOptions = {
  value: string;
  language: string;
  nodeLimit: number;
  groupByLine?: boolean;
};

const useSyntaxHighlighting = ({ value, language, nodeLimit, groupByLine = false }: UseSyntaxHighlightingOptions) => {
  const job = useRef(nanoid());
  const [isLoading, setIsLoading] = useState(true);
  const [tree, setTree] = useState<Array<RefractorNode> | undefined>(undefined);
  const [error, setError] = useState<string | undefined>(undefined);

  useEffect(() => {
    setIsLoading(true);
    setError(undefined);
    setTree(undefined);

    const payload: HighlightingRequest["payload"] = {
      value,
      language,
      nodeLimit,
      groupByLine,
    };

    worker.postMessage({
      id: job.current,
      payload,
    } as HighlightingRequest);

    const listener = ({ data }: ResponseMessage) => {
      if (data.id === job.current) {
        if (isSuccess(data)) {
          setTree(data.payload.tree);
        } else {
          setError(data.payload.reason);
        }
      }
      setIsLoading(false);
    };

    worker.addEventListener("message", listener);
    return () => worker.removeEventListener("message", listener);
  }, [value, language, nodeLimit]);

  return {
    tree,
    isLoading,
    error,
  };
};

export default useSyntaxHighlighting;
