/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC, useState } from "react";

import { PrismAsyncLight as ReactSyntaxHighlighter } from "react-syntax-highlighter";
import { defaultLanguage, determineLanguage } from "./languages";
// eslint-disable-next-line no-restricted-imports
import highlightingTheme from "./syntax-highlighting";
import { useLocation } from "react-router-dom";
import { urls } from "@scm-manager/ui-api";
import createSyntaxHighlighterRenderer from "./SyntaxHighlighterRenderer";
import useScrollToElement from "./useScrollToElement";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import copyToClipboard from "./CopyToClipboard";

const LINE_NUMBER_URL_HASH_REGEX = /^#line-(.*)$/;

const TopRightButton = styled.button`
  position: absolute;
  display: none;
  top: 0;
  right: 0;
`;

const Container = styled.div`
  position: relative;
  &:hover > ${TopRightButton} {
    display: inline-block;
  }
`;

type Props = {
  language?: string;
  value: string;
  showLineNumbers?: boolean;
  permalink?: string;
};

const SyntaxHighlighter: FC<Props> = ({ language = defaultLanguage, showLineNumbers = true, value, permalink }) => {
  const location = useLocation();
  const [contentRef, setContentRef] = useState<HTMLElement | null>();
  const [copied, setCopied] = useState(false);
  const [t] = useTranslation("commons");

  useScrollToElement(
    contentRef,
    () => {
      const match = location.hash.match(LINE_NUMBER_URL_HASH_REGEX);
      if (match) {
        return `#line-${match[1]}`;
      }
    },
    value
  );

  const copy = () => {
    copyToClipboard(value).then(() => setCopied(true));
  };

  const createLinePermaLink = (lineNumber: number) =>
    window.location.protocol +
    "//" +
    window.location.host +
    urls.withContextPath((permalink || location.pathname) + "#line-" + lineNumber);

  const defaultRenderer = createSyntaxHighlighterRenderer(createLinePermaLink, showLineNumbers);

  let valueWithoutTrailingLineBreak = value;
  if (value && value.length > 1 && value.endsWith("\n")) {
    valueWithoutTrailingLineBreak = value.substr(0, value.length - 1);
  }

  return (
    <Container ref={setContentRef}>
      <ReactSyntaxHighlighter
        showLineNumbers={false}
        language={determineLanguage(language)}
        style={highlightingTheme}
        renderer={defaultRenderer}
      >
        {valueWithoutTrailingLineBreak}
      </ReactSyntaxHighlighter>
      <TopRightButton title={t("syntaxHighlighting.copyButton")} onClick={copy}>
        <i className={copied ? "fa fa-clipboard-check" : "fa fa-clipboard"} />
      </TopRightButton>
    </Container>
  );
};

export default SyntaxHighlighter;
