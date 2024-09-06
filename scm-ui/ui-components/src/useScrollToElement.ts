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
          const element = contentRef.querySelector(elementSelector);
          if (element) {
            const headerElement = document.querySelector(".navbar-brand");
            const margin = headerElement ? headerElement.getBoundingClientRect().height : 45;
            clearInterval(intervalId);
            const y = element.getBoundingClientRect().top + window.pageYOffset - margin;
            window.scrollTo({top: y});
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
