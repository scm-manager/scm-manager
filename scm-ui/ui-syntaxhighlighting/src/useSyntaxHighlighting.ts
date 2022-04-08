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
import type { RefractorRoot } from "refractor";

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

worker.postMessage({ theme });

type PayloadSuccess = {
  success: true;
  tree: any;
  html: string;
};

type PayloadFailure = {
  success: false;
  reason: string;
};

type Payload = PayloadSuccess | PayloadFailure;

const isSuccess = (payload: Payload): payload is PayloadSuccess => {
  return payload.success;
};

type Message = {
  data: {
    id: string;
    payload: Payload;
  };
};

const useSyntaxHighlighting = (value: string, language: string, nodeLimit: number) => {
  const job = useRef(nanoid());
  const [isLoading, setIsLoading] = useState(true);
  const [tree, setTree] = useState<RefractorRoot | undefined>(undefined);
  const [error, setError] = useState<string | undefined>(undefined);

  useEffect(() => {
    setIsLoading(true);
    setError(undefined);
    setTree(undefined);

    worker.postMessage({
      id: job.current,
      payload: {
        value,
        language,
        nodeLimit,
      },
    });

    const listener = ({ data }: Message) => {
      if (data.id === job.current) {
        if (isSuccess(data.payload)) {
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
