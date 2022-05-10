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
import React, { FC, Dispatch, useRef } from "react";
import styled from "styled-components";
import AuthorImage from "./AuthorImage";
import DateShort from "../../DateShort";
import { Action } from "./actions";
import { AnnotatedLine } from "@scm-manager/ui-types";

const LineElement = styled.div`
  display: inline-block;
  margin: 0;
  padding: 0;
  height: 100%;
  vertical-align: top;
`;

const Author = styled(LineElement)`
  width: 8em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const When = styled(LineElement)`
  width: 6.5em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  margin: 0 0.5em;
`;

const LineNumber = styled(LineElement)`
  width: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  // TODO: ERSETZEN?
  border-left: 1px solid lightgrey;
  border-right: 1px solid lightgrey;

  text-align: right;

  padding: 0 0.5em;
`;

const Line = styled.div`
  margin: 0;
  padding: 0;
  height: 1.5em;
  vertical-align: top;
`;

const Metadata = styled(LineElement)`
  cursor: help;
  width: 15.5em;
`;

const EmptyMetadata = styled(LineElement)`
  width: 15.5em; // width of author + when
`;

const dispatchDeferred = (dispatch: Dispatch<Action>, action: Action) => {
  setTimeout(() => dispatch(action), 250);
};

type Props = {
  annotation: AnnotatedLine;
  showAnnotation: boolean;
  nr: number;
  dispatch: Dispatch<Action>;
};

const AnnotateLine: FC<Props> = ({ annotation, showAnnotation, dispatch, nr, children }) => {
  const link = useRef<HTMLDivElement>(null);

  const onMouseEnter = () => {
    if (showAnnotation) {
      dispatchDeferred(dispatch, {
        annotation,
        line: nr,
        offset: link.current?.offsetTop || 0,
        type: "enter-line",
      });
    }
  };

  const OnMouseLeave = () => {
    if (showAnnotation) {
      dispatchDeferred(dispatch, {
        line: nr,
        type: "leave-line",
      });
    }
  };

  if (!showAnnotation) {
    return (
      <Line>
        <EmptyMetadata />
        <LineNumber>{nr}</LineNumber> <LineElement>{children}</LineElement>
      </Line>
    );
  }

  return (
    <Line>
      <Metadata className="has-text-info" onMouseOver={onMouseEnter} onMouseOut={OnMouseLeave} ref={link}>
        <Author>
          <AuthorImage person={annotation.author} />
          {annotation.author.name}
        </Author>{" "}
        <When>
          <DateShort date={annotation.when} />
        </When>{" "}
      </Metadata>
      <LineNumber>{nr}</LineNumber> <LineElement>{children}</LineElement>
    </Line>
  );
};

export default AnnotateLine;
