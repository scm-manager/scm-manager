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

import lowlight from "lowlight/lib/core";

// adapter to let lowlight look like refractor
// this is required because react-diff-view does only support refractor,
// but we want same highlighting as in the source code browser.

const isLanguageRegistered = (lang: string) => {
  // @ts-ignore listLanguages seems unknown to type
  const registeredLanguages = lowlight.listLanguages();
  return !!registeredLanguages[lang];
};

const loadLanguage = (lang: string, callback: () => void) => {
  if (isLanguageRegistered(lang)) {
    callback();
  } else {
    import(
      /* webpackChunkName: "tokenizer-lowlight-[request]" */
      `highlight.js/lib/languages/${lang}`
    ).then(loadedLanguage => {
      lowlight.registerLanguage(lang, loadedLanguage.default);
      callback();
    });
  }
};

const refractorAdapter = {
  ...lowlight,
  isLanguageRegistered,
  loadLanguage,
  highlight: (value: string, language: string) => {
    return lowlight.highlight(language, value).value;
  }
};

export default refractorAdapter;
