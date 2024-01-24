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

import React from "react";
import classNames from "classnames";

type Props = React.HTMLProps<HTMLElement> & {
  children?: string;
};

/**
 * Icons are hidden to assistive technologies by default.
 *
 * If your icon does convey a state, unset `aria-hidden` and set an appropriate `aria-label`.
 *
 * The children have to be a single text node containing a valid fontawesome icon name.
 *
 * @beta
 * @since 2.44.0
 * @see https://bulma.io/documentation/elements/icon/
 * @see https://fontawesome.com/search?o=r&m=free
 */
const Icon = React.forwardRef<HTMLElement, Props>(({ children, className, ...props }, ref) => {
  return (
    <span className={classNames(className, "icon")} aria-hidden="true" {...props} ref={ref}>
      <i
        className={classNames(`fas fa-fw fa-${children}`, {
          "fa-xs": className?.includes("is-small"),
          "fa-lg": className?.includes("is-medium"),
          "fa-2x": className?.includes("is-large"),
        })}
      />
    </span>
  );
});

export default Icon;
