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

import React from "react";
import classNames from "classnames";

type Props = React.HTMLProps<HTMLElement> & {
  children?: string;
  type?: string;
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
const Icon = React.forwardRef<HTMLElement, Props>(({ children, className, type = "fas", ...props }, ref) => {
  return (
    <span className={classNames(className, "icon")} aria-hidden="true" {...props} ref={ref}>
      <i
        className={classNames(`${type} fa-fw fa-${children}`, {
          "fa-xs": className?.includes("is-small"),
          "fa-lg": className?.includes("is-medium"),
          "fa-2x": className?.includes("is-large"),
        })}
      />
    </span>
  );
});

export default Icon;
