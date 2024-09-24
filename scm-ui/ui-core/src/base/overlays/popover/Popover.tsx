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
import * as RadixPopover from "@radix-ui/react-popover";
import styled from "styled-components";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

const StyledContent = styled(RadixPopover.Content)`
  z-index: 1020;
`;

const StyledArrow = styled(RadixPopover.Arrow)`
  fill: var(--scm-popover-border-color);
  z-index: 1020;
`;

const TitleContainer = styled("div")`
  flex: 1;
`;

type Props = {
  /**
   * Element to trigger the popover
   */
  trigger: ReactElement;

  /**
   * Element for the content of the popover
   */
  children: ReactElement;

  /**
   * Element for the title row of the popover
   */
  title: ReactNode;

  /**
   * Classnames for the content of the popover
   */
  className?: string;
};

/**
 * @beta
 * @since 2.46.0
 */
const Popover = React.forwardRef<HTMLDivElement, Props>(({ title, className, trigger, children }, ref) => {
  const [t] = useTranslation("commons");

  return (
    <RadixPopover.Root>
      <RadixPopover.Trigger asChild>{trigger}</RadixPopover.Trigger>
      <RadixPopover.Portal>
        <StyledContent
          ref={ref}
          className={classNames("has-rounded-border", "p-2", "popover-content", "box", "popover", className)}
        >
          <div className="is-flex is-align-items-center">
            <TitleContainer className="mr-4">{title}</TitleContainer>
            <RadixPopover.Close asChild>
              <button className="delete popover-close" aria-label={t("popover.closeButton.ariaLabel")} />
            </RadixPopover.Close>
          </div>
          {children}
          <StyledArrow className="popover-arrow" />
        </StyledContent>
      </RadixPopover.Portal>
    </RadixPopover.Root>
  );
});

export default Popover;
