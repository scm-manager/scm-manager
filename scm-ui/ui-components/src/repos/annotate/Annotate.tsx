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

import React, { FC, useReducer } from "react";
import { AnnotatedLine, AnnotatedSource, Repository } from "@scm-manager/ui-types";
import { DateInput } from "../../useDateFormatter";
import AnnotatePopover from "./AnnotatePopover";
import AnnotateLine from "./AnnotateLine";
import { Action } from "./actions";
import { determineLanguage } from "../../languages";
import { SyntaxHighlighter } from "@scm-manager/ui-syntaxhighlighting";

type Props = {
  source: AnnotatedSource;
  repository: Repository;
  baseDate?: DateInput;
};

type State = {
  annotation?: AnnotatedLine;
  offset?: number;
  line?: number;
  onPopover: boolean;
  onLine: boolean;
};

const initialState = {
  onPopover: false,
  onLine: false,
};

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "enter-line": {
      if (state.onPopover) {
        return state;
      }
      return {
        annotation: action.annotation,
        offset: action.offset,
        line: action.line,
        onLine: true,
        onPopover: false,
      };
    }
    case "leave-line": {
      if (state.onPopover) {
        return {
          ...state,
          onLine: false,
        };
      }
      return initialState;
    }
    case "enter-popover": {
      return {
        ...state,
        onPopover: true,
      };
    }
    case "leave-popover": {
      if (state.onLine) {
        return {
          ...state,
          onPopover: false,
        };
      }
      return initialState;
    }
  }
};

const Annotate: FC<Props> = ({ source, repository, baseDate }) => {
  const [state, dispatch] = useReducer(reducer, initialState);

  const defaultRenderer: FC = ({ children }: any) => {
    let lastRevision = "";
    const rows = React.Children.toArray(children);
    return (
      <>
        {rows.map((line: React.ReactNode, i: number) => {
          const annotation = source.lines[i];
          if (annotation) {
            const newAnnotation = annotation.revision !== lastRevision;
            lastRevision = annotation.revision;
            return (
              <AnnotateLine
                key={`line-${i}`}
                dispatch={dispatch}
                annotation={annotation}
                showAnnotation={newAnnotation}
                nr={i + 1}
              >
                {line}
              </AnnotateLine>
            );
          }
          return line;
        })}
      </>
    );
  };

  let popover = null;
  if ((state.onPopover || state.onLine) && state.annotation) {
    popover = (
      <AnnotatePopover
        annotation={state.annotation}
        dispatch={dispatch}
        offsetTop={state.offset}
        repository={repository}
        baseDate={baseDate}
      />
    );
  }

  const code = source.lines.reduce((content, line) => {
    content += line.code + "\n";
    return content;
  }, "");

  return (
    <div className="panel-block">
      {popover}
      <SyntaxHighlighter value={code} language={determineLanguage(source.language)} renderer={defaultRenderer} />
    </div>
  );
};

export default Annotate;
