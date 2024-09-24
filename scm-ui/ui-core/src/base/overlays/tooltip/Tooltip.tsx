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

import React, { ReactElement, ReactNode } from "react";
import * as RadixTooltip from "@radix-ui/react-tooltip";
import classNames from "classnames";
import styled from "styled-components";

const StyledContent = styled(RadixTooltip.Content)`
  overflow: hidden;
  hyphens: auto;
  text-overflow: clip;
  white-space: pre-wrap;
  max-width: 15rem;
  word-break: keep-all;
  z-index: 1020;
`;

const StyledArrow = styled(RadixTooltip.Arrow)`
  z-index: 1020;
`;

type Props = {
  /**
   * The message to be displayed in the overlay.
   */
  message: ReactNode;

  /**
   * It is required to provide a {@link ReactElement} as the single child because listeners and metadata is going to be
   * attached to it automatically.
   */
  children: ReactElement;

  /**
   * Class to be applied to the content container.
   */
  className?: string;
} & Pick<RadixTooltip.TooltipContentProps, "side">;

/**
 * Displays the given {@link Props#message} whenever the provided {@link Props#children} is hovered or focused.
 *
 * @since 2.41.0
 * @beta
 */
const Tooltip = React.forwardRef<HTMLDivElement, Props>(({ children, className, message, side }, ref) => (
  <RadixTooltip.Provider>
    <RadixTooltip.Root>
      <RadixTooltip.Trigger asChild>{children}</RadixTooltip.Trigger>
      <RadixTooltip.Portal>
        <StyledContent
          className={classNames(
            "is-size-7",
            "is-family-primary",
            "has-rounded-border",
            "has-text-white",
            "has-background-grey-dark",
            "has-text-weight-semibold",
            "p-2",
            className
          )}
          side={side}
          sideOffset={4}
          collisionPadding={4}
          ref={ref}
        >
          {message}
          <StyledArrow className="tooltip-arrow-fill-color" />
        </StyledContent>
      </RadixTooltip.Portal>
    </RadixTooltip.Root>
  </RadixTooltip.Provider>
));
export default Tooltip;
