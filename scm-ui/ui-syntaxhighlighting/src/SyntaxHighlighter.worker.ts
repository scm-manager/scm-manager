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
import type { RefractorElement } from "refractor";
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

const countChildren = (node: RefractorNode) => (isRefractorElement(node) ? countNodes(node.children) : 1);
const countNodes = (nodes: RefractorNode[]): number => nodes.reduce((count, node) => count + countChildren(node), 0);

function doHighlighting({ id, payload: { value, language, nodeLimit, groupByLine } }: HighlightingRequest) {
  const highlightContent = (worker: Worker) => {
    try {
      // console.time("highlight");
      let tree = refractor.highlight(value, language).children;
      // console.timeEnd("highlight");
      if (groupByLine) {
        // console.time("groupByLines");
        try {
          tree = groupByLines(tree, nodeLimit);
        } catch (e) {
          const payload: FailureResponse["payload"] = {
            reason: e instanceof Error ? e.message : `node limit of ${nodeLimit} reached.`,
          };
          worker.postMessage({ id, type: "failure", payload } as FailureResponse);
          return;
        } finally {
          // console.timeEnd("groupByLines");
        }
      }

      if (nodeLimit > 0) {
        // console.time("countNodes");
        const count = countNodes(tree);
        // console.timeEnd("countNodes");
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
