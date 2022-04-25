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

import React, { FC, useEffect, useState } from "react";
import Icon from "./Icon";
import Tooltip from "./Tooltip";
import styled from "styled-components";
import copyToClipboard from "./CopyToClipboard";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";

const RowContainer = styled.div`
  .linenumber {
    display: inline-block;
    min-width: 3em;
    padding-right: 1em;
    text-align: right;
    user-select: none;
    color: rgb(154, 154, 154);
  }
  span.linenumber:hover {
    cursor: pointer;
  }
  span.linenumber + span > span.linenumber {
    display: none !important;
  }
  &.focused,
  &.focused > span:last-child {
    background-color: rgb(229, 245, 252);
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
                <span
                  onClick={() => history.push(location.pathname + "#line-" + lineNumber)}
                  className="linenumber react-syntax-highlighter-line-number"
                >
                  {lineNumber}
                </span>
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
