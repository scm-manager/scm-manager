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
import { Person } from "@scm-manager/ui-types";

// @ts-ignore
import { LightAsync as ReactSyntaxHighlighter, createElement } from "react-syntax-highlighter";

// @ts-ignore
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";
import styled from "styled-components";
import DateShort from "./DateShort";

// TODO move types to ui-types

export type AnnotatedSource = {
  lines: AnnotatedLine[];
  language: string;
};

export type AnnotatedLine = {
  author: Person;
  code: string;
  description: string;
  lineNumber: number;
  revision: string;
  when: Date;
};

type Props = {
  source: AnnotatedSource;
};

const Author = styled.a`
  width: 8em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  float: left;
`;

const LineNumber = styled.span`
  width: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  border-left: 1px solid lightgrey;
  border-right: 1px solid lightgrey;

  text-align: right;
  float: left;

  padding: 0 0.5em;
`;

const When = styled.span`
  width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  float: left;

  margin: 0 0.5em;
`;

const Annotate: FC<Props> = ({ source }) => {
  // @ts-ignore
  const defaultRenderer = ({ rows, stylesheet, useInlineStyles }) => {
    // @ts-ignore
    return rows.map((node, i) => {
      const line = createElement({
        node,
        stylesheet,
        useInlineStyles,
        key: `code-segement${i}`
      });

      if (i + 1 < rows.length) {
        const annotation = source.lines[i];
        return (
          <span>
            <Author>{annotation.author.name}</Author>{" "}
            <When>
              <DateShort value={annotation.when} />
            </When>{" "}
            <LineNumber>{i + 1}</LineNumber> {line}
          </span>
        );
      }

      return line;
    });
  };

  const code = source.lines.reduce((content, line) => {
    content += line.code + "\n";
    return content;
  }, "");

  return (
    <ReactSyntaxHighlighter
      showLineNumbers={false}
      language={source.language}
      style={arduinoLight}
      renderer={defaultRenderer}
    >
      {code}
    </ReactSyntaxHighlighter>
  );
};

export default Annotate;
