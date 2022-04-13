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

import type { RefractorElement, Text } from "refractor/lib/core";

export type Message<T> = { data: T };
export type MessageData<T extends string, P> = { id: string; type: T; payload: P };

export type RefractorNode = RefractorElement | Text;
export type Theme = Record<string, string>;

export type MarkerBounds = {
  start: string;
  end: string;
};

export type LoadThemeRequest = MessageData<"theme", Theme>;

export type HighlightingRequest = MessageData<
  "highlight",
  {
    language: string;
    value: string;
    nodeLimit: number;
    groupByLine: boolean;
    markedTexts?: string[];
  }
>;

export type TokenizeRequest = MessageData<
  "tokenize",
  {
    language: string;
    hunks: any;
  }
>;

export type Request = LoadThemeRequest | HighlightingRequest | TokenizeRequest;
export type RequestMessage = Message<Request>;

export type SuccessResponse = MessageData<"success", { tree: Array<RefractorNode> }>;

export type FailureResponse = MessageData<"failure", { reason: string }>;

export type Response = SuccessResponse | FailureResponse;
export type ResponseMessage = Message<Response>;

export function isRefractorElement(node: RefractorNode): node is RefractorElement {
  return (node as RefractorElement).tagName !== undefined;
}

export type TokenizeSuccessResponse = {
  id: string;
  payload: {
    success: true;
    tokens: {
      old: Array<RefractorNode>;
      new: Array<RefractorNode>;
    };
  };
};

export type TokenizeFailureResponse = {
  id: string;
  payload: {
    success: false;
    reason: string;
  };
};
