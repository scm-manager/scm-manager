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

import type { RefractorElement, Text } from "refractor";
import createRefractor, { RefractorAdapter } from "./refractorAdapter";

type RefractorNode = RefractorElement | Text;

type Theme = { [key: string]: string };

type LoadThemeMessage = {
  theme: Theme;
};

type HighlightingMessage = {
  id: string;
  payload: {
    language: string;
    value: string;
    nodeLimit: number;
  };
};

type Message = {
  data: LoadThemeMessage | HighlightingMessage;
};

const isLoadThemeMessage = (message: LoadThemeMessage | HighlightingMessage): message is LoadThemeMessage => {
  return !!(message as LoadThemeMessage).theme;
};

// the WorkerGlobalScope is assigned to self
// see https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/self
declare const self: Worker;

let refractor: RefractorAdapter;

function initRefractor(theme: { [key: string]: string }) {
  refractor = createRefractor(theme);
}

function isRefractorElement(node: RefractorNode): node is RefractorElement {
  return (node as RefractorElement).tagName !== undefined;
}

function countChildren(node: RefractorNode) {
  if (isRefractorElement(node)) {
    let count = node.children.length;
    node.children.forEach((child) => (count += countChildren(child)));
    return count;
  } else {
    return 1;
  }
}

function countNodes(nodes: RefractorNode[]) {
  let count = 0;
  nodes.forEach((node) => (count += countChildren(node)));
  return count;
}

function doHighlighting({ id, payload }: HighlightingMessage) {
  const { value, language, nodeLimit } = payload;

  const highlightContent = (worker: Worker) => {
    try {
      const tree = refractor.highlight(value, language);

      if (nodeLimit > 0) {
        const count = countNodes(tree.children);
        if (count > nodeLimit) {
          const payload = {
            success: false,
            reason: `node limit of ${nodeLimit} reached. Total nodes ${count}.`,
          };
          worker.postMessage({ id, payload });
        } else {
          const payload = {
            success: true,
            tree,
            count,
            nodeLimit,
          };
          worker.postMessage({ id, payload });
        }
      }
    } catch (ex) {
      const payload = {
        success: false,
        reason: ex,
      };
      worker.postMessage({ id, payload });
    }
  };

  if (language !== "text") {
    refractor.loadLanguage(language, () => highlightContent(self));
  }
}

self.addEventListener("message", ({ data }: Message) => {
  if (isLoadThemeMessage(data)) {
    initRefractor(data.theme);
  } else {
    doHighlighting(data);
  }
});
