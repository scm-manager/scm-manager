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

export type Request = LoadThemeRequest | HighlightingRequest;
export type RequestMessage = Message<Request>;

export type SuccessResponse = MessageData<"success", { tree: Array<RefractorNode> }>;

export type FailureResponse = MessageData<"failure", { reason: string }>;

export type Response = SuccessResponse | FailureResponse;
export type ResponseMessage = Message<Response>;

export function isRefractorElement(node: RefractorNode): node is RefractorElement {
  return (node as RefractorElement).tagName !== undefined;
}
