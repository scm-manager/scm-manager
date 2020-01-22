import React, { FC } from "react";
import styled from "styled-components";
// @ts-ignore we have no typings for react-diff-view
import { Diff, useTokenizeWorker } from "react-diff-view";
// @ts-ignore we use webpack worker-loader to load the web worker
import TokenizeWorker from "./Tokenize.worker";
import { File } from "./DiffTypes";

// styling for the diff tokens
// this must be aligned with th style, which is used in the SyntaxHighlighter component
import "highlight.js/styles/arduino-light.css";

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
  /* smaller font size for code */
  & .diff-line {
    font-size: 0.75rem;
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

// WebWorker which creates tokens for syntax highlighting
const tokenize = new TokenizeWorker();

type Props = {
  file: File;
  viewType: "split" | "unified";
  className?: string;
};

const determineLanguage = (file: File) => {
  if (file.language) {
    return file.language.toLowerCase();
  }
  return "text";
};

const TokenizedDiffView: FC<Props> = ({ file, viewType, className, children }) => {
  const { tokens } = useTokenizeWorker(tokenize, {
    hunks: file.hunks,
    language: determineLanguage(file)
  });

  return (
    <DiffView className={className} viewType={viewType} tokens={tokens} hunks={file.hunks} diffType={file.type}>
      {children}
    </DiffView>
  );
};

export default TokenizedDiffView;
