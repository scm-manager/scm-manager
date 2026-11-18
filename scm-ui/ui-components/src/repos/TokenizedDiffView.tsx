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

import React, { FC, ReactElement } from "react";
import styled from "styled-components";
import { Diff, DiffProps, HunkData, useTokenizeWorker } from "react-diff-view";
import { FileDiff, Hunk as HunkType } from "@scm-manager/ui-types";

import { determineLanguage } from "../languages";
import { useSyntaxHighlightingWorker } from "@scm-manager/ui-syntaxhighlighting";
import classNames from "classnames";

type StyleProps = {
  hunkGutterHoverIcon?: string;
  actionable?: boolean;
  highlightLineOnHover?: boolean;
};

const DiffView = styled(Diff)<StyleProps>`
  /* align line numbers */

  & .diff-gutter {
    text-align: right;
  }

  /* column sizing */

  > colgroup .diff-gutter-col {
    width: 3.25rem;
  }

  /* prevent following content from moving down */

  > .diff-gutter:empty:hover::after {
    font-size: 0.7rem;
  }

  /* smaller font size for code and
  ensure same monospace font throughout whole scmm */

  & .diff-line {
    font-size: 0.75rem;
    font-family: "Courier New", Monaco, Menlo, "Ubuntu Mono", "source-code-pro", monospace;
  }

  /* comment padding for sidebyside view */

  &.split .diff-widget-content .is-indented-line {
    padding-left: 3.25rem;
  }

  /* comment padding for combined view */

  &.unified .diff-widget-content .is-indented-line {
    padding-left: 6.5rem;
  }

  ${(props) => {
    let style = props.hunkGutterHoverIcon
      ? `
    .diff-gutter:hover::after {
      font-size: inherit;
      margin-left: 0.5em;
      font-family: "Font Awesome 5 Free";
      content: "${props.hunkGutterHoverIcon}";
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

type Props = StyleProps &
  Pick<DiffProps, "widgets" | "gutterEvents" | "generateLineClassName"> & {
    file: FileDiff;
    viewType: "split" | "unified";
    className?: string;
    whitespace: boolean;
    children: (hunks: HunkType[]) => ReactElement[];
  };

const findSyntaxHighlightingLanguage = (file: FileDiff) => {
  if (file.syntaxModes) {
    return file.syntaxModes["prism"] || file.syntaxModes["codemirror"] || file.syntaxModes["ace"] || file.language;
  }
  return file.language;
};
const TokenizedDiffView: FC<Props> = ({ file, viewType, className, children, whitespace, ...diffProps }) => {
  const worker = useSyntaxHighlightingWorker();
  const hunks = file.hunks as HunkData[];
  const { tokens } = useTokenizeWorker(worker, {
    hunks,
    oldSource: null,
    language: determineLanguage(findSyntaxHighlightingLanguage(file)),
    whitespace: whitespace,
  });
  const diffClassName = tokens ? className : classNames(className, "diff-tokenizing");

  return (
    <DiffView
      className={diffClassName}
      viewType={viewType}
      tokens={tokens}
      hunks={hunks}
      diffType={file.type}
      aria-busy={!tokens}
      {...diffProps}
    >
      {children}
    </DiffView>
  );
};

export default TokenizedDiffView;
