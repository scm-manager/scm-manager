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

import React, { FC, useCallback, useMemo, useState } from "react";
import { defaultLanguage, determineLanguage } from "./languages";
import { useLocation } from "react-router-dom";
import { urls } from "@scm-manager/ui-api";
import createSyntaxHighlighterRenderer from "./SyntaxHighlighterRenderer";
import useScrollToElement from "./useScrollToElement";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import copyToClipboard from "./CopyToClipboard";
import { Button } from "./buttons";
import { SyntaxHighlighter as NewSyntaxHighlighter } from "@scm-manager/ui-syntaxhighlighting";

const LINE_NUMBER_URL_HASH_REGEX = /^#line-(.*)$/;

const TopRightButton = styled(Button)`
  height: inherit;
  position: absolute;
  display: none;
  top: 0;
  right: 0;
`;

const Container = styled.div`
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
  const createLinePermaLink = useCallback(
    (lineNumber: number) =>
      window.location.protocol +
      "//" +
      window.location.host +
      urls.withContextPath((permalink || location.pathname) + "#line-" + lineNumber),
    [permalink, location]
  );
  const Renderer = useMemo(
    () => createSyntaxHighlighterRenderer(createLinePermaLink, showLineNumbers),
    [createLinePermaLink, showLineNumbers]
  );

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

  const copy = () => copyToClipboard(value).then(() => setCopied(true));

  let valueWithoutTrailingLineBreak = value;
  if (value && value.length > 1 && value.endsWith("\n")) {
    valueWithoutTrailingLineBreak = value.substr(0, value.length - 1);
  }
  return (
    <Container ref={setContentRef} className="is-relative">
      <NewSyntaxHighlighter
        value={valueWithoutTrailingLineBreak}
        language={determineLanguage(language)}
        renderer={Renderer}
      />
      <TopRightButton className="is-small" title={t("syntaxHighlighting.copyButton")} action={copy}>
        <i className={copied ? "fa fa-clipboard-check" : "fa fa-clipboard"} />
      </TopRightButton>
    </Container>
  );
};

export default SyntaxHighlighter;
