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
import React, { HTMLAttributes, useEffect } from "react";
import classNames from "classnames";

type Props = {
  title?: string;
  customPageTitle?: string;
  preventRefreshingPageTitle?: boolean;
};
const Title = React.forwardRef<HTMLHeadingElement, HTMLAttributes<HTMLHeadingElement> & Props>(
  ({ title, customPageTitle, preventRefreshingPageTitle, children, className, ...props }, ref) => {
    useEffect(() => {
      if (!preventRefreshingPageTitle) {
        if (customPageTitle) {
          document.title = customPageTitle;
        } else if (title) {
          document.title = title;
        }
      }
    }, [title, preventRefreshingPageTitle, customPageTitle]);

    if (children || title) {
      return (
        <h1 className={classNames("title", className)} {...props} ref={ref}>
          {title}
          {children}
        </h1>
      );
    }
    return null;
  }
);

export default Title;
