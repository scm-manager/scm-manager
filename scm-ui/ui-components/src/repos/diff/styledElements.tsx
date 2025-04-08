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

import styled from "styled-components";
// @ts-ignore react-diff-view does not provide types
import { Hunk } from "react-diff-view";
import { Icon } from "@scm-manager/ui-core";
import { devices } from "../../devices";

export type Collapsible = {
  collapsed?: boolean;
};

export const StyledHunk = styled(Hunk)`
  ${(props) => {
    let style = props.icon
      ? `
    .diff-gutter:hover::after {
      font-size: inherit;
      margin-left: 0.5em;
      font-family: "Font Awesome 5 Free";
      content: "${props.icon}";
      color: var(--scm-column-selection);
    }
  `
      : "";
    if (!props.actionable) {
      style += `
      .diff-gutter {
        cursor: default;
      }
    `;
    }
    if (props.highlightLineOnHover) {
      style += `
      tr.diff-line:hover > td {
        background-color: var(--sh-selected-color);
      }
    `;
    }
    return style;
  }}
`;

export const DiffFilePanel = styled.div<Collapsible>`
  /* remove bottom border for collapsed panels */
  ${(props: Collapsible) => (props.collapsed ? "border-bottom: none;" : "")};
`;

export const FullWidthTitleHeader = styled.div`
  max-width: 100%;
`;

export const MarginlessModalContent = styled.div`
  margin: -1.25rem;

  & .panel-block {
    flex-direction: column;
    align-items: stretch;
  }
`;

export const PanelHeading = styled.div<{ sticky?: boolean | number }>`
  ${(props) => {
    if (props.sticky) {
      return `
   position: sticky;
   top: calc(${typeof props.sticky === "number" ? props.sticky : 0}px + var(--scm-navbar-main-height));
   z-index: 1;
  `;
    }
  }}
`;

export const StickyFileDiffContainer = styled.div`
  top: 5rem;
  position: sticky;
  height: 100%;

  @media screen and (max-width: ${devices.mobile.width}px) {
    top: 0;
    position: relative;
    flex: 0 0 100%;
    margin-bottom: 0.5rem;
  }
`;

export const FileTreeContent = styled.div<{ isBorder: boolean }>`
  ${({ isBorder }) =>
    isBorder &&
    `
    border: 1px solid var(--scm-border-color);
    border-radius: 0.25rem;
    overflow: hidden;
  `}
`;

export const DiffTreeTitle = styled.h3`
  border-bottom: 1px solid var(--scm-border-color);
  box-shadow: 0 24px 3px -24px var(--scm-border-color);
`;

export const DiffContent = styled.div`
  width: 100%;
  padding-top: 0;
`;

export const FileDiffContent = styled.ul<{ gap?: number }>`
  overflow: auto;
  ${(props) => {
    if (props.gap) {
      return `
     @supports (-moz-appearance: none) {
    max-height: calc(100vh - ${props.gap}rem);
  }
  max-height: calc(100svh - ${props.gap}rem);
  `;
    }
  }};
`;

export const StackedSpan = styled.span`
  width: 3em;
  height: 3em;
  font-size: 0.5em;
`;

export const StyledIcon = styled(Icon)<{ isSmaller?: boolean }>`
  ${({ isSmaller }) =>
    isSmaller &&
    `
      font-size: 0.5em;
      margin-top: 0.05rem;
  `}
  min-width: 1.5rem;
`;
