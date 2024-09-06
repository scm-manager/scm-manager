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
