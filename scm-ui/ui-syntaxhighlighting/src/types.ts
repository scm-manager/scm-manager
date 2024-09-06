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
    hunks: unknown;
    whitespace?: boolean;
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
