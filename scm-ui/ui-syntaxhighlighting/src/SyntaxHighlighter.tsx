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

import React, { ComponentType, ReactNode, useMemo } from "react";
import useSyntaxHighlighting from "./useSyntaxHighlighting";
import type { RefractorElement } from "refractor";
import type { Element } from "hast";
import type { RefractorNode } from "./types";

// marker
// line wrapper

type LineWrapperType = ComponentType<{ lineNumber: number }>;

export type Props = {
  value: string;
  language?: string;
  lineWrapper?: LineWrapperType;
};

function mapWithDepth(depth: number, lineWrapper?: LineWrapperType) {
  return function mapChildrenWithDepth(child: RefractorNode, i: number) {
    return mapChild(child, i, depth, lineWrapper);
  };
}

const isRefractorElement = (node: RefractorNode): node is RefractorElement => "tagName" in node;

function mapChild(childNode: RefractorNode, i: number, depth: number, LineWrapper?: LineWrapperType): ReactNode {
  if (isRefractorElement(childNode)) {
    const child = childNode as Element;
    const className =
      child.properties &&
      (Array.isArray(child.properties.className) ? child.properties.className.join(" ") : child.properties.className);

    if (LineWrapper && child.properties) {
      const line = child.properties["data-line-number"];
      if (line) {
        return (
          <LineWrapper key={`line-${depth}-${i}`} lineNumber={Number(line)}>
            {childNode.children && childNode.children.map(mapWithDepth(depth + 1, LineWrapper))}
          </LineWrapper>
        );
      }
    }

    return React.createElement(
      child.tagName,
      Object.assign({ key: `fract-${depth}-${i}` }, child.properties, { className }),
      childNode.children && childNode.children.map(mapWithDepth(depth + 1))
    );
  }

  return <React.Fragment key={`content-${depth}-${i}`}>{childNode.value}</React.Fragment>;
}

const createFallbackContent = (value: string, LineWrapper?: LineWrapperType): ReactNode => {
  if (LineWrapper) {
    return value.split("\n").map((line, i) => (
      <LineWrapper key={i} lineNumber={i + 1}>
        {line}
      </LineWrapper>
    ));
  }
  return value;
};

const SyntaxHighlighter = ({ value, lineWrapper, language = "text" }: Props) => {
  // TODO error
  const { isLoading, tree } = useSyntaxHighlighting({ value, language, nodeLimit: 1000, groupByLine: !!lineWrapper });
  const fallbackContent = useMemo(() => createFallbackContent(value, lineWrapper), [value, lineWrapper]);

  if (isLoading || !tree) {
    // if (true) {
    return (
      <pre>
        <code className={`language-${language}`}>{fallbackContent}</code>
      </pre>
    );
  }

  return (
    <pre>
      <code className={`language-${language}`}>{tree?.map(mapWithDepth(0, lineWrapper))}</code>
    </pre>
  );
};

export default SyntaxHighlighter;
