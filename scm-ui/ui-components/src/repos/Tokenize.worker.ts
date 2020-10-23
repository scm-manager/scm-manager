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

// @ts-ignore we have no types for react-diff-view
import { tokenize } from "react-diff-view";
import createRefractor, { RefractorAdapter } from "./refractorAdapter";

// the WorkerGlobalScope is assigned to self
// see https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/self
declare const self: Worker;

type TokenizeMessage = {
  id: string;
  language: string;
  hunks: any;
  payload: any;
};

let refractor: RefractorAdapter;

function initRefractor(theme: { [key: string]: string }) {
  refractor = createRefractor(theme);
}

function runTokenize({ id, payload }: TokenizeMessage) {
  const { hunks, language } = payload;

  const options = {
    highlight: language !== "text",
    language: language,
    refractor
  };

  const doTokenization = (worker: Worker) => {
    try {
      const tokens = tokenize(hunks, options);
      const payload = {
        success: true,
        tokens: tokens
      };
      worker.postMessage({ id, payload });
    } catch (ex) {
      const payload = {
        success: false,
        reason: ex.message
      };
      worker.postMessage({ id, payload });
    }
  };

  const createTokenizer = (worker: Worker) => () => doTokenization(worker);

  if (options.highlight) {
    refractor.loadLanguage(language, createTokenizer(self));
  }
}

self.addEventListener("message", ({ data }) => {
  if (data.theme) {
    initRefractor(data.theme);
  } else {
    runTokenize(data);
  }
});
