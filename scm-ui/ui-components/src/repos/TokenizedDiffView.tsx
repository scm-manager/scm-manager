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

import React, { FC } from "react";
import styled from "styled-components";
// @ts-ignore we have no typings for react-diff-view
import { Diff, useTokenizeWorker } from "react-diff-view";
import { FileDiff } from "@scm-manager/ui-types";
import { determineLanguage } from "../languages";
import { useSyntaxHighlightingWorker } from "@scm-manager/ui-syntaxhighlighting";

const DiffView = styled(Diff)`
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
`;

type Props = {
  file: FileDiff;
  viewType: "split" | "unified";
  className?: string;
  whitespace: boolean;
};

const findSyntaxHighlightingLanguage = (file: FileDiff) => {
  if (file.syntaxModes) {
    return file.syntaxModes["prism"] || file.syntaxModes["codemirror"] || file.syntaxModes["ace"] || file.language;
  }
  return file.language;
};
const TokenizedDiffView: FC<Props> = ({ file, viewType, className, children, whitespace }) => {
  const worker = useSyntaxHighlightingWorker();
  const { tokens } = useTokenizeWorker(worker, {
    hunks: file.hunks,
    language: determineLanguage(findSyntaxHighlightingLanguage(file)),
    whitespace: whitespace,
  });

  return (
    <DiffView className={className} viewType={viewType} tokens={tokens} hunks={file.hunks} diffType={file.type}>
      {children}
    </DiffView>
  );
};

export default TokenizedDiffView;
