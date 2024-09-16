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

import { useEffect } from "react";

const useScrollToElement = (
  contentRef: HTMLElement | null | undefined,
  elementSelectorResolver: () => string | undefined,
  ...dependencies: any
) => {
  useEffect(() => {
    const elementSelector = elementSelectorResolver();
    if (contentRef && elementSelector) {
      // We defer the content check until after the syntax-highlighter has rendered
      let tries = 0;
      const intervalId = setInterval(() => {
        if (tries >= 15) {
          clearInterval(intervalId);
        } else {
          tries++;
          const element = contentRef.querySelector(CSS.escape(elementSelector));
          if (element) {
            const headerElement = document.querySelector(".navbar-brand");
            const margin = headerElement ? headerElement.getBoundingClientRect().height : 45;
            clearInterval(intervalId);
            const y = element.getBoundingClientRect().top + window.pageYOffset - margin;
            window.scrollTo({ top: y });
          }
        }
      }, 200);

      return () => {
        clearInterval(intervalId);
      };
    }
  }, [contentRef, ...dependencies]);
};

export default useScrollToElement;
