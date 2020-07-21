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
import React, { FC } from "react";
import styled from "styled-components";
// @ts-ignore
import { LightAsync as ReactSyntaxHighlighter, createElement } from "react-syntax-highlighter";
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";

type Props = {
  language: string;
  value: string;
  showLineNumbers?: boolean;
};

const SingleLine = styled.div`
  height: 1.5em;
`;

const InnerLineNumber = styled.div`
  display: inline-block;
  padding: 0 0.5em;
  width: 3.25rem;
  border-right: 1px solid lightgrey;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border-right: 1px solid lightgrey;
  text-align: right;
  font-family: "Courier New", Monaco, Menlo, "Ubuntu Mono", source-code-pro, monospace;
`;

const InnerLineContent = styled.div`
  display: inline-block;
  padding: 0 0.5em;
  vertical-align: top;
`;

const LineNumbers: FC<Props> = ({ language, value, showLineNumbers }) => {
  const defaultRenderer = ({ rows, stylesheet, useInlineStyles }: any) => {
    return rows.map((node: React.ReactNode, i: number) => {
      const line = createElement({
        node,
        stylesheet,
        useInlineStyles,
        key: `code-segment${i}`
      });

      if (i + 1 < rows.length) {
        return (
          <SingleLine key={i}>
            {showLineNumbers && <InnerLineNumber>{i + 1}</InnerLineNumber>}
            <InnerLineContent>{line}</InnerLineContent>
          </SingleLine>
        );
      }
      return null;
    });
  };

  return (
    <div style={{ position: "relative" }}>
      <ReactSyntaxHighlighter
        showLineNumbers={false}
        language={language ? language : "text"}
        style={arduinoLight}
        renderer={defaultRenderer}
      >
        {value}
      </ReactSyntaxHighlighter>
    </div>
  );
};

LineNumbers.defaultProps = {
  showLineNumbers: true
};

export default LineNumbers;
