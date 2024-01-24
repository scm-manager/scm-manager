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
import React, { FC, useEffect } from "react";
import classNames from "classnames";

type Props = {
  title?: string;
  customPageTitle?: string;
  preventRefreshingPageTitle?: boolean;
  className?: string;
};

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

const Title: FC<Props> = ({ title, preventRefreshingPageTitle, customPageTitle, className, children }) => {
  useEffect(() => {
    if (!preventRefreshingPageTitle) {
      if (customPageTitle) {
        document.title = customPageTitle;
      } else if (title) {
        document.title = title;
      }
    }
  }, [title, preventRefreshingPageTitle, customPageTitle]);

  if (children) {
    return <h1 className={classNames("title", className)}>{children}</h1>;
  } else if (title) {
    return <h1 className={classNames("title", className)}>{title}</h1>;
  }
  return null;
};

Title.defaultProps = {
  preventRefreshingPageTitle: false,
};

export default Title;
