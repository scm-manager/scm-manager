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

import groupByLines from "./groupByLines";
// @ts-ignore we have no types for react-diff-view
import { tokenize } from "react-diff-view";
import createRefractor, { RefractorAdapter } from "./refractorAdapter";
import type {
  FailureResponse,
  HighlightingRequest,
  LoadThemeRequest,
  RefractorNode,
  Request,
  RequestMessage,
  SuccessResponse,
  Theme,
  TokenizeFailureResponse,
  TokenizeRequest,
  TokenizeSuccessResponse,
} from "../types";
import { isRefractorElement } from "../types";
import type { RefractorElement } from "refractor";

// the WorkerGlobalScope is assigned to self
// see https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/self
declare const self: Worker;

let refractor: RefractorAdapter;

function initRefractor(theme: Theme) {
  refractor = createRefractor(theme);
}

const countChildrenAndApplyMarkers = (node: RefractorNode, markedTexts?: string[]) => {
  if (isRefractorElement(node)) {
    return countAndMarkNodes(node.children, markedTexts);
  }

  if (!markedTexts || markedTexts.length === 0) {
    return 1;
  }

  let content = node.value;
  const newChildren: RefractorNode[] = [];
  while (content.length) {
    let foundSomething = false;
    for (const markedText of markedTexts) {
      const start = content.indexOf(markedText);
      if (start >= 0) {
        foundSomething = true;
        const end = start + markedText.length;
        newChildren.push({ type: "text", value: content.substring(0, start) });
        newChildren.push({
          type: "element",
          tagName: "mark",
          properties: {
            "data-marked": true,
          },
          children: [{ type: "text", value: content.substring(start, end) }],
        });
        content = content.substring(end);
      }
    }
    if (!foundSomething) {
      break;
    }
  }

  if (content.length) {
    newChildren.push({ type: "text", value: content });
  }

  if (newChildren.length > 0) {
    const el = node as unknown as RefractorElement;
    el.type = "element";
    el.tagName = "span";
    el.children = newChildren;
    return newChildren.length + 1;
  }

  return 1;
};
const countAndMarkNodes = (nodes: RefractorNode[], markedTexts?: string[]): number =>
  nodes.reduce((count, node) => count + countChildrenAndApplyMarkers(node, markedTexts), 0);

const doHighlighting = (
  worker: Worker,
  { id, payload: { value, language, nodeLimit, groupByLine, markedTexts } }: HighlightingRequest
) => {
  try {
    let tree = refractor.highlight(value, language).children;
    if (groupByLine) {
      try {
        tree = groupByLines(tree, nodeLimit);
      } catch (e) {
        const payload: FailureResponse["payload"] = {
          reason: e instanceof Error ? e.message : `node limit of ${nodeLimit} reached.`,
        };
        worker.postMessage({ id, type: "failure", payload } as FailureResponse);
        return;
      }
    }

    if (nodeLimit > 0) {
      const count = countAndMarkNodes(tree, markedTexts);
      if (count > nodeLimit) {
        const payload: FailureResponse["payload"] = {
          reason: `node limit of ${nodeLimit} reached. Total nodes ${count}.`,
        };
        worker.postMessage({ id, type: "failure", payload } as FailureResponse);
      } else {
        const payload: SuccessResponse["payload"] = {
          tree,
        };
        worker.postMessage({ id, type: "success", payload } as SuccessResponse);
      }
    }
  } catch (ex) {
    const payload: FailureResponse["payload"] = {
      reason: String(ex),
    };
    worker.postMessage({ id, type: "failure", payload } as FailureResponse);
  }
};

const isLoadThemeMessage = (message: Request): message is LoadThemeRequest => {
  return message.type === "theme";
};

const isTokenizeMessage = (message: Request): message is TokenizeRequest => {
  return message.type === "tokenize";
};

const TOKENIZE_NODE_LIMIT = 600;

const replaceSpaces = (node: RefractorNode): RefractorNode => {
  if (node?.type === "text") {
    if (node.value.indexOf(" ") < 0 && node.value.indexOf("\t") < 0) {
      return node;
    }
    return {
      type: "element",
      tagName: "span",
      properties: {},
      children: node.value.split("").flatMap((c) => {
        if (c === " ") {
          return [
            {
              type: "element",
              tagName: "span",
              children: [{ type: "text", value: " " }],
              properties: {
                className: ["space_char"],
              },
            },
            {
              type: "element",
              tagName: "span",
              children: [{ type: "text", value: "" }],
              properties: {},
            },
          ];
        } else if (c === "\t") {
          return [
            {
              type: "element",
              tagName: "span",
              children: [{ type: "text", value: "\t" }],
              properties: {
                className: ["tabulator_char"],
              },
            },
            {
              type: "element",
              tagName: "span",
              children: [{ type: "text", value: "" }],
              properties: {},
            },
          ];
        } else {
          return {
            type: "element",
            tagName: "span",
            properties: {},
            children: [{ type: "text", value: c }],
          };
        }
      }),
    };
  } else if (node?.type === "element") {
    return {
      ...node,
      children: node.children.map(replaceSpaces),
    };
  }
  return node;
};

const runTokenize = ({ id, payload }: TokenizeRequest) => {
  const { hunks, language, whitespace } = payload;

  const options = {
    highlight: true, // we always want to run refractor to enable the display of whitespace characters
    language: language,
    refractor: {
      ...refractor,
      highlight: (value: string, lang: string) => {
        if (whitespace) {
          return refractor.highlight(value, lang).children.map(replaceSpaces);
        }
        return refractor.highlight(value, lang).children;
      },
    },
  };

  const doTokenization = (worker: Worker) => {
    try {
      const tokens: { old: RefractorNode[]; new: RefractorNode[] } = tokenize(hunks, options);
      const tokensCount = countAndMarkNodes(tokens.old) + countAndMarkNodes(tokens.new);
      if (tokensCount > TOKENIZE_NODE_LIMIT) {
        const response: TokenizeFailureResponse = {
          id,
          payload: {
            success: false,
            reason: `Node limit (${TOKENIZE_NODE_LIMIT}) reached. Current nodes: ${tokensCount}`,
          },
        };
        worker.postMessage(response);
      } else {
        const response: TokenizeSuccessResponse = {
          id,
          payload: {
            success: true,
            tokens,
          },
        };
        worker.postMessage(response);
      }
    } catch (ex) {
      const response: TokenizeFailureResponse = {
        id,
        payload: {
          success: false,
          reason: String(ex),
        },
      };
      worker.postMessage(response);
    }
  };

  const createTokenizer = (worker: Worker) => () => doTokenization(worker);

  if (options.highlight) {
    refractor.loadLanguage(language, createTokenizer(self));
  }
};

self.addEventListener("message", ({ data }: RequestMessage) => {
  if (isLoadThemeMessage(data)) {
    initRefractor(data.payload);
  } else if (isTokenizeMessage(data)) {
    runTokenize(data);
  } else if (data.payload.language !== "text") {
    refractor.loadLanguage(data.payload.language, () => doHighlighting(self, data));
  }
});
