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
import { Person, Repository } from "@scm-manager/ui-types";

// @ts-ignore
import { LightAsync as ReactSyntaxHighlighter, createElement } from "react-syntax-highlighter";
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";
import styled from "styled-components";
import DateShort from "./DateShort";
import { SingleContributor } from "./repos/changesets";
import DateFromNow from "./DateFromNow";
import { Link } from "react-router-dom";

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
  repository: Repository;
};

type LineElementProps = {
  newAnnotation: boolean;
};

const Author = styled.span<LineElementProps>`
  width: 8em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  float: left;

  visibility: ${({ newAnnotation }) => (newAnnotation ? "visible" : "hidden")};
`;

const When = styled.span<LineElementProps>`
  width: 6.5em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  float: left;

  margin: 0 0.5em;
  visibility: ${({ newAnnotation }) => (newAnnotation ? "visible" : "hidden")};
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

const Popover = styled.div`
  position: absolute;
  left: -16.5em;
  bottom: 0.1em;

  z-index: 100;
  visibility: hidden;
  overflow: visible;

  width: 35em;

  &:before {
    position: absolute;
    content: "";
    border-style: solid;
    pointer-events: none;
    height: 0;
    width: 0;
    top: 100%;
    left: 5.5em;
    border-color: transparent;
    border-bottom-color: white;
    border-left-color: white;
    border-width: 0.4rem;
    margin-left: -0.4rem;
    margin-top: -0.4rem;
    -webkit-transform-origin: center;
    transform-origin: center;
    box-shadow: -1px 1px 2px rgba(10, 10, 10, 0.2);
    transform: rotate(-45deg);
  }
`;

const Line = styled.span`
  position: relative;
  z-index: 10;

  &:hover .changeset-details {
    visibility: visible !important;
  }
`;

const PreTag = styled.pre`
  overflow-x: visible !important;
`;

const SmallHr = styled.hr`
  margin: 0.5em 0;
`;

const PopoverHeading = styled.div`
  height: 1.5em;
`;

const PopoverDescription = styled.p`
  margin-top: 0.5em;
`;

const shortRevision = (revision: string) => {
  if (revision.length > 7) {
    return revision.substring(0, 7);
  }
  return revision;
};

const Annotate: FC<Props> = ({ source, repository }) => {
  const defaultRenderer = ({ rows, stylesheet, useInlineStyles }: any) => {
    let lastRevision = "";
    return rows.map((node: any, i: number) => {
      const line = createElement({
        node,
        stylesheet,
        useInlineStyles,
        key: `code-segement${i}`
      });

      if (i + 1 < rows.length) {
        const annotation = source.lines[i];
        const newAnnotation = annotation.revision !== lastRevision;
        lastRevision = annotation.revision;
        return (
          <Line>
            <Popover className="box changeset-details is-family-primary">
              <PopoverHeading className="is-clearfix">
                <SingleContributor className="is-pulled-left" person={annotation.author} />
                <DateFromNow className="is-pulled-right" date={annotation.when} />
              </PopoverHeading>
              <SmallHr />
              <p className="has-text-info">Changeset {shortRevision(annotation.revision)}</p>
              <PopoverDescription className="content">{annotation.description}</PopoverDescription>
            </Popover>
            <Author className="trigger" newAnnotation={newAnnotation}>
              <Link to={`/repo/${repository.namespace}/${repository.name}/code/changeset/${annotation.revision}`}>
                {annotation.author.name}
              </Link>
            </Author>{" "}
            <When newAnnotation={newAnnotation}>
              <DateShort value={annotation.when} />
            </When>{" "}
            <LineNumber>{i + 1}</LineNumber> {line}
          </Line>
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
      PreTag={PreTag}
    >
      {code}
    </ReactSyntaxHighlighter>
  );
};

export default Annotate;
