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

import React, { ComponentProps, ReactNode, useEffect, useState } from "react";
import * as RadixCollapsible from "@radix-ui/react-collapsible";
import { Icon } from "../../buttons";
import styled from "styled-components";
import { useAriaId } from "../../helpers";
import classNames from "classnames";

const StyledTrigger = styled(RadixCollapsible.Trigger)`
  margin-right: 0.5rem;
`;

const StyledCollapsibleHeader = styled.div`
  background-color: var(--scm-secondary-less-color);
`;

type Props = {
  header: ReactNode;
  defaultCollapsed?: boolean;
  collapsed?: boolean;
  onCollapsedChange?: (collapsed: boolean) => void;
} & Pick<ComponentProps<typeof RadixCollapsible.Root>, "className" | "children">;

/**
 * @beta
 * @since 2.46.0
 */
const Collapsible = React.forwardRef<HTMLButtonElement, Props>(
  ({ children, header, className, defaultCollapsed, collapsed, onCollapsedChange }, ref) => {
    const [isCollapsed, setCollapsed] = useState(defaultCollapsed);
    const titleId = useAriaId();
    useEffect(() => {
      if (collapsed !== undefined) {
        setCollapsed(collapsed);
      }
    }, [collapsed]);

    return (
      <RadixCollapsible.Root
        className={classNames("card", className)}
        open={!isCollapsed}
        onOpenChange={(o) => {
          setCollapsed(!o);
          if (onCollapsedChange) {
            onCollapsedChange(!o);
          }
        }}
        defaultOpen={!defaultCollapsed}
      >
        <StyledCollapsibleHeader className="card-header is-flex is-justify-content-space-between is-shadowless">
          <span id={titleId} className="card-header-title">
            {header}
          </span>
          <StyledTrigger aria-labelledby={titleId} className="card-header-icon" ref={ref}>
            <Icon>{isCollapsed ? "angle-left" : "angle-down"}</Icon>
          </StyledTrigger>
        </StyledCollapsibleHeader>
        <RadixCollapsible.Content className="card-content p-2">{children}</RadixCollapsible.Content>
      </RadixCollapsible.Root>
    );
  }
);

export default Collapsible;
