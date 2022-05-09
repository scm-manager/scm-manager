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
