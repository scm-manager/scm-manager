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
