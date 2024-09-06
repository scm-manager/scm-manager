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

import React, { useState } from "react";
import Icon from "../../buttons/Icon";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  message: string;
  summary: string;
  className?: string;
}

const StyledButton = styled.button`
  border-radius: 4px !important;
  min-width: 2.5rem;  
  min-height: 24px;
  border: none;
  padding: 6px;
  line-height: 15px;
  &:not(:disabled) {
    &:hover {
      background-color: color-mix(in srgb, currentColor 25%, transparent);
    }
    &:active {
      background-color: color-mix(in srgb, currentColor 50%, transparent);
    }
  }
`;



/**
 * An a11y-friendly tooltip replacement that is an expandable hint
 *
 * @beta
 * @since 3.2.0
 */

const ExpandableHint = React.forwardRef<HTMLButtonElement, Props>(({ message, summary, className }, ref) => {
  const [isOpen, setOpen] = useState<boolean>(false);
  return (
    <>
      <StyledButton
        role="button"
        onClick={() => setOpen(!isOpen)}
        aria-expanded={isOpen}
        aria-label={summary}
        className={classNames(className, "is-flex")}
        ref={ref}
      >
          {isOpen ? <Icon className="is-small">chevron-down</Icon> : <Icon className="is-small">chevron-right</Icon>}
        {summary}
      </StyledButton>
      {isOpen ? <p className="mt-2 is-size-7">{message}</p> : undefined}
    </>
  );
});

export default ExpandableHint;
