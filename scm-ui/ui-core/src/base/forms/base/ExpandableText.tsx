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

import React, { HTMLAttributes, FC, Ref } from "react";
import classNames from "classnames";
import styled from "styled-components";
import Icon from "../../buttons/Icon";

/**
 * Keeps the native `<details>`/`<summary>` behaviour (native toggle, keyboard support, `aria-expanded`)
 * but replaces the browser's default disclosure marker with our own icon: the native marker is hidden and
 * a custom `Icon` is rendered inside the summary, rotated via the `[open]` state.
 */
const StyledDetails = styled.details`
  & > summary {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    cursor: pointer;
    list-style: none; /* hides the marker in Firefox */
  }

  /* hides the marker in Chrome/Safari */
  & > summary::-webkit-details-marker {
    display: none;
  }

  & > summary .icon {
    transition: transform 0.2s ease;
  }

  &[open] > summary .icon {
    transform: rotate(90deg);
  }
`;

interface Props extends HTMLAttributes<HTMLDetailsElement> {
  descriptionText: string;
  extendedDescriptionText: string;
  id?: string;
  /**
   * Fontawesome icon name used as the disclosure marker. It is rotated 90° while the details are open,
   * so a directional icon (the default `chevron-right`) points right when collapsed and down when expanded.
   */
  icon?: string;
  ref?: Ref<HTMLDetailsElement>;
}

/**
 * Displays a single line of text by default and allows users to expand it to view the full content.
 *
 * The description text is shown initially with a custom disclosure icon to toggle the visibility of the
 * extended text. The underlying element is still a native `<details>`.
 *
 * @param descriptionText - The text content that will always be displayed.
 * @param extendedDescriptionText - The text content that will be displayed after you toggle details.
 * @param id - Passed to the `<details>` element.
 * @param icon - Overrides the disclosure icon (fontawesome name); defaults to `chevron-right`.
 * @param ref - A ref to the `<details>` element.
 *
 * @beta
 * @since 3.9.0
 */
const ExpandableText: FC<Props> = ({
  descriptionText,
  extendedDescriptionText,
  id,
  icon = "chevron-right",
  className,
  ref,
  ...rest
}) => {
  return (
    <StyledDetails className={classNames("mb-2 is-size-7", className)} id={id} ref={ref} {...rest}>
      <summary>
        <Icon className="is-small">{icon}</Icon>
        <span>{descriptionText}</span>
      </summary>
      <span>{extendedDescriptionText}</span>
    </StyledDetails>
  );
};

export default ExpandableText;
