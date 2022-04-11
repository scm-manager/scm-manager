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
import type { RefractorElement, Text } from "refractor";
import createRefractor, { RefractorAdapter } from "./refractorAdapter";
import type {
  FailureResponse,
  HighlightingRequest,
  LoadThemeRequest,
  MarkerBounds,
  RefractorNode,
  Request,
  RequestMessage,
  SuccessResponse,
  Theme,
} from "./types";

// the WorkerGlobalScope is assigned to self
// see https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/self
declare const self: Worker;

let refractor: RefractorAdapter;

function initRefractor(theme: Theme) {
  refractor = createRefractor(theme);
}

function isRefractorElement(node: RefractorNode): node is RefractorElement {
  return (node as RefractorElement).tagName !== undefined;
}

const countChildren = (node: RefractorNode, markerBounds?: MarkerBounds) => {
  if (isRefractorElement(node)) {
    return countAndMarkNodes(node.children, markerBounds);
  } else {
    if (markerBounds) {
      const { start: preTag, end: postTag } = markerBounds;
      let content = node.value;
      const newChildren = [];
      while (content.length) {
        const start = content.indexOf(preTag);
        const end = content.indexOf(postTag);
        if (start >= 0 && end >= 0) {
          newChildren.push(content.substring(0, start));
          newChildren.push(content.substring(start + preTag.length, end));
          content = content.substring(end + postTag.length);
        } else {
          break;
        }
      }
      if (newChildren.length > 0) {
        node.children = newChildren.map<Text>((value) => ({ type: "text", value }));
        (node as any).type = "element";
        node.tagName = "mark";
        node.properties = {
          ["data-marked"]: true,
        };
        return newChildren.length + 1;
      }
    }
    return 1;
  }
};
const countAndMarkNodes = (nodes: RefractorNode[], markerBounds?: MarkerBounds): number =>
  nodes.reduce((count, node) => count + countChildren(node, markerBounds), 0);

function doHighlighting({
  id,
  payload: { value, language, nodeLimit, groupByLine, markerBounds },
}: HighlightingRequest) {
  const highlightContent = (worker: Worker) => {
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
        const count = countAndMarkNodes(tree, markerBounds);
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

  if (language !== "text") {
    refractor.loadLanguage(language, () => highlightContent(self));
  }
}

const isLoadThemeMessage = (message: Request): message is LoadThemeRequest => {
  return message.type === "theme";
};

self.addEventListener("message", ({ data }: RequestMessage) => {
  if (isLoadThemeMessage(data)) {
    initRefractor(data.payload);
  } else {
    doHighlighting(data);
  }
});
