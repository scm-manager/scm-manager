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

import React, { FC, useEffect, useState } from "react";
import Icon from "./Icon";
import Tooltip from "./Tooltip";
import styled from "styled-components";
import copyToClipboard from "./CopyToClipboard";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";

const RowContainer = styled.div`
  span.linenumber + span > span.linenumber {
    display: none !important;
  }
  &.focused > span.linenumber {
    box-shadow: inset -3px 0 0 var(--scm-sh-focus-line-contrast);
  }
  &.focused,
  &.focused > span:last-child {
    background: var(--scm-sh-focus-line-background);
  }
  i {
    visibility: hidden;
  }
  i:hover {
    cursor: pointer;
  }
  &:hover i {
    visibility: visible;
  }
  // override bulma default styling of .section
  .section {
    padding: 0;
  }
  & > span:last-child {
    margin-left: 0.75em;
  }
`;

const LineNumber = styled.span<{ value: number }>`
  display: inline-block;
  min-width: 3em;
  padding-right: 0.75em;
  text-align: right;
  color: var(--scm-secondary-text);
  &:hover {
    cursor: pointer;
  }
  &::before {
    content: "${({ value }) => `${value}`}";
  }
`;

type CreateLinePermaLinkFn = (lineNumber: number) => string;

type ExternalProps = {
  createLinePermaLink: CreateLinePermaLinkFn;
  showLineNumbers: boolean;
};

type BaseProps = {
  children: React.ReactChild[];
};

type Props = ExternalProps & BaseProps;

const SyntaxHighlighterRenderer: FC<Props> = ({ children: rows, createLinePermaLink, showLineNumbers = true }) => {
  const location = useLocation();
  const history = useHistory();
  const [focusedLine, setLineToFocus] = useState<number | undefined>(undefined);
  const [copying, setCopying] = useState(false);
  const [t] = useTranslation("repos");

  useEffect(() => {
    const match = location.hash.match(/^#line-(.*)$/);
    if (match) {
      const lineNumber = match[1];
      setLineToFocus(Number(lineNumber));
    }
  }, [location.hash]);

  const lineNumberClick = (lineNumber: number) => {
    history.push(location.pathname + "#line-" + lineNumber);
    setCopying(true);
    copyToClipboard(createLinePermaLink(lineNumber)).finally(() => setCopying(false));
  };

  return (
    <>
      {rows.map((line: React.ReactNode, i: number) => {
        const lineNumber = i + 1;
        return (
          <RowContainer
            id={`line-${lineNumber}`}
            className={(focusedLine === lineNumber && "focused") || undefined}
            key={`line-${lineNumber}`}
          >
            {showLineNumbers && (
              <>
                {copying ? (
                  <Icon name="spinner fa-spin" alt={t("sources.content.loading")} />
                ) : (
                  <Tooltip message={t("sources.content.copyPermalink")}>
                    <Icon
                      name="link"
                      onClick={() => lineNumberClick(lineNumber)}
                      alt={t("sources.content.copyPermalink")}
                    />
                  </Tooltip>
                )}
                <LineNumber
                  onClick={() => history.push(location.pathname + "#line-" + lineNumber)}
                  className="linenumber react-syntax-highlighter-line-number"
                  value={lineNumber}
                />
              </>
            )}
            {line}
          </RowContainer>
        );
      })}
    </>
  );
};

//
export const create = (createLinePermaLink: CreateLinePermaLinkFn, showLineNumbers = false): FC<BaseProps> => {
  return (props) => (
    <SyntaxHighlighterRenderer {...props} createLinePermaLink={createLinePermaLink} showLineNumbers={showLineNumbers} />
  );
};

export default create;
