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

import React, { ComponentType, HTMLAttributes, ReactHTML, Ref } from "react";
import classNames from "classnames";
import styled from "styled-components";

const RowsContainer = styled.div`
  margin-left: -0.25rem;
`;

type Gap = "inherit" | "initial" | "normal" | "-moz-initial" | "revert" | "unset" | string | number;

type Props = HTMLAttributes<HTMLElement> & {
  action?: React.ReactElement;
  avatar?: React.ReactElement;
  /**
   * @default "div"
   */
  as?: keyof ReactHTML | ComponentType<HTMLAttributes<HTMLElement> & { ref?: Ref<HTMLElement> }>;

  /**
   * @default "0.5rem"
   * @since 2.46.0
   */
  rowGap?: Gap;
};

/**
 * If the Card's title contains a link, the whole Card becomes a click target for that link.
 * Because of this, any interactive elements require the <code>is-relative</code> class to receive cursor events.
 *
 * @beta
 * @since 2.44.0
 */
const Card = React.forwardRef<HTMLElement, Props>(
  ({ className, avatar, rowGap = "0.25rem", children, as: Comp = "div", action, ...props }, ref) =>
    React.createElement(
      Comp,
      {
        className: classNames(className, "is-relative", "is-flex", "scmm-card"),
        ref,
        ...props,
      },
      avatar ? avatar : null,
      <RowsContainer
        className="is-flex is-flex-direction-column is-justify-content-center is-flex-grow-1 is-overflow-hidden is-overflow-wrap-anywhere"
        style={{ gap: rowGap }}
      >
        {children}
      </RowsContainer>,
      action ? action : null
    )
);

export default Card;
