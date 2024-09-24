/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { useEffect, useRef, useState } from "react";

import { nanoid } from "nanoid";
import type { HighlightingRequest, RefractorNode, Response, ResponseMessage, SuccessResponse } from "./types";
import useSyntaxHighlightingWorker from "./useSyntaxHighlightingWorker";

const isSuccess = (response: Response): response is SuccessResponse => {
  return response.type === "success";
};

export type UseSyntaxHighlightingOptions = {
  value: string;
  language: string;
  nodeLimit: number;
  groupByLine?: boolean;
  markedTexts?: string[];
};

const useSyntaxHighlighting = ({
  value,
  language,
  nodeLimit,
  groupByLine = false,
  markedTexts,
}: UseSyntaxHighlightingOptions) => {
  const worker = useSyntaxHighlightingWorker();
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
      markedTexts,
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
  }, [value, language, nodeLimit, groupByLine, worker, markedTexts]);

  return {
    tree,
    isLoading,
    error,
  };
};

export default useSyntaxHighlighting;
