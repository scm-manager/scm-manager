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

// eslint-disable-next-line @typescript-eslint/ban-ts-ignore
// @ts-ignore
import { createElement, PrismAsyncLight as ReactSyntaxHighlighter } from "react-syntax-highlighter";
import { defaultLanguage, determineLanguage } from "./languages";
// eslint-disable-next-line no-restricted-imports
import highlightingTheme from "./syntax-highlighting";
import styled from "styled-components";
import { useLocation } from "react-router-dom";
import { withContextPath } from "./urls";
import { Toast } from "./toast";

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
  &.focused {
    background-color: blue;
  }
`;

type Props = {
  language?: string;
  value: string;
  showLineNumbers?: boolean;
  createPermaLink?: () => string;
};

const SyntaxHighlighter: FC<Props> = ({
  language = defaultLanguage,
  showLineNumbers = true,
  value,
  createPermaLink
}) => {
  const location = useLocation();
  const [rowToHighlight, setRowToHighlight] = useState<number | undefined>(undefined);
  const [contentRef, setContentRef] = useState<HTMLElement | null>();
  const [copySuccess, setCopySuccess] = useState<boolean>(false);

  useEffect(() => {
    const hash = location.hash;
    const match = hash && hash.match(/^#line-(.*)$/);
    if (contentRef && match) {
      const lineNumber = match[1];
      setRowToHighlight(Number(lineNumber));
      // We defer the content check until after the syntax-highlighter has rendered
      setTimeout(() => {
        const element = contentRef.querySelector(`#line-${lineNumber}`);
        if (element && element.scrollIntoView) {
          element.scrollIntoView();
        }
      });
    }
  }, [value, contentRef, location.hash]);

  const createLinePermaLink = (lineNumber: number) =>
    window.location.protocol +
    "//" +
    window.location.host +
    withContextPath(((createPermaLink && createPermaLink()) || location.pathname) + "#line-" + lineNumber);

  const lineNumberClick = (lineNumber: number) => copyToClipboard(createLinePermaLink(lineNumber));

  function copyToClipboard(text: string) {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).then(
        function() {
          setCopySuccess(true);
        },
        function(err) {
          setCopySuccess(false);
        }
      );
    } else {
      const textArea = document.createElement("textarea");
      textArea.value = text;
      textArea.style.position = "fixed"; //avoid scrolling to bottom
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();

      try {
        document.execCommand("copy");
        setCopySuccess(true);
      } catch (err) {
        setCopySuccess(false);
      }

      document.body.removeChild(textArea);
    }
  }

  // eslint-disable-next-line @typescript-eslint/ban-ts-ignore
  // @ts-ignore
  const defaultRenderer = ({ rows, stylesheet, useInlineStyles }) => {
    return rows.map((node: React.ReactNode, i: number) => {
      const lineNumber = i + 1;
      const line = createElement({
        node,
        stylesheet,
        useInlineStyles,
        key: `code-segment${i}`
      });
      return (
        <RowContainer
          id={`line-${lineNumber}`}
          className={(rowToHighlight === lineNumber && "focused") || undefined}
          key={`line-${lineNumber}`}
        >
          {showLineNumbers && (
            <a
              className="linenumber react-syntax-highlighter-line-number"
              onClick={() => lineNumberClick(lineNumber)}
              href={withContextPath(location.pathname + "#line-" + lineNumber)}
            >
              {lineNumber}
            </a>
          )}
          {line}
        </RowContainer>
      );
    });
  };

  return (
    <div ref={setContentRef}>
      <ReactSyntaxHighlighter
        showLineNumbers={false}
        language={determineLanguage(language)}
        style={highlightingTheme}
        renderer={defaultRenderer}
      >
        {value}
      </ReactSyntaxHighlighter>
      {copySuccess && <Toast type="success" title={"Copied"} />}
    </div>
  );
};

export default SyntaxHighlighter;
