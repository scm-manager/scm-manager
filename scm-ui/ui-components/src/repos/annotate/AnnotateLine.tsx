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
        offset: link.current!.offsetTop,
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
