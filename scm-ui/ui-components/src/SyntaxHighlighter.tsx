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
import { Icon, IconButton } from "@scm-manager/ui-core";
import { SyntaxHighlighter as NewSyntaxHighlighter } from "@scm-manager/ui-syntaxhighlighting";

const LINE_NUMBER_URL_HASH_REGEX = /^#line-(.*)$/;

// Opaque background, so the code below the transparent icon button stays covered
const TopRightCorner = styled.div`
  position: absolute;
  display: none;
  top: 0;
  right: 0;
  border-radius: 6px;
  background-color: var(--scm-secondary-background);
`;

const Container = styled.div`
  &:hover > ${TopRightCorner} {
    display: flex;
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
      <TopRightCorner>
        <IconButton
          title={t("syntaxHighlighting.copyButton")}
          aria-label={t("syntaxHighlighting.copyButton")}
          onClick={copy}
        >
          <Icon>{copied ? "clipboard-check" : "clipboard"}</Icon>
        </IconButton>
      </TopRightCorner>
    </Container>
  );
};

export default SyntaxHighlighter;
