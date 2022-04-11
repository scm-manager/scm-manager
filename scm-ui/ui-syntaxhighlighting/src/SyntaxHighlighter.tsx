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
import { MarkerBounds } from "./types";
import { SplitAndReplace } from "@scm-manager/ui-text";

type LineWrapperType = ComponentType<{ lineNumber: number }>;
type MarkerConfig = MarkerBounds & {
  wrapper: ComponentType;
};

type Replacement = {
  textToReplace: string;
  replacement: ReactNode;
  replaceAll: boolean;
};

export type Props = {
  value: string;
  language?: string;
  lineWrapper?: LineWrapperType;
  markerConfig?: MarkerConfig;
};

function mapWithDepth(depth: number, lineWrapper?: LineWrapperType, markerReplacement?: ComponentType) {
  return function mapChildrenWithDepth(child: RefractorNode, i: number) {
    return mapChild(child, i, depth, lineWrapper, markerReplacement);
  };
}

const isRefractorElement = (node: RefractorNode): node is RefractorElement => "tagName" in node;

function mapChild(
  childNode: RefractorNode,
  i: number,
  depth: number,
  LineWrapper?: LineWrapperType,
  MarkerReplacement?: ComponentType
): ReactNode {
  if (isRefractorElement(childNode)) {
    const child = childNode as Element;
    const className =
      child.properties &&
      (Array.isArray(child.properties.className) ? child.properties.className.join(" ") : child.properties.className);

    if (child.properties) {
      if (LineWrapper) {
        const line = child.properties["data-line-number"];
        if (line) {
          return (
            <LineWrapper key={`line-${depth}-${i}`} lineNumber={Number(line)}>
              {childNode.children && childNode.children.map(mapWithDepth(depth + 1, LineWrapper, MarkerReplacement))}
            </LineWrapper>
          );
        }
      }
      if (MarkerReplacement) {
        const isMarked = child.properties["data-marked"];
        if (isMarked) {
          return (
            <MarkerReplacement>
              {childNode.children && childNode.children.map(mapWithDepth(depth + 1, LineWrapper, MarkerReplacement))}
            </MarkerReplacement>
          );
        }
      }
    }

    return React.createElement(
      child.tagName,
      Object.assign({ key: `fract-${depth}-${i}` }, child.properties, { className }),
      childNode.children && childNode.children.map(mapWithDepth(depth + 1, LineWrapper, MarkerReplacement))
    );
  }

  return <React.Fragment key={`content-${depth}-${i}`}>{childNode.value}</React.Fragment>;
}

function escapeRegExp(str: string) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"); // $& means the whole matched string
}

const stripAndReplace = (value: string, { start: preTag, end: postTag, wrapper: Wrapper }: MarkerConfig) => {
  const strippedValue = value
    .replace(new RegExp(escapeRegExp(preTag), "g"), "")
    .replace(new RegExp(escapeRegExp(postTag), "g"), "");
  let content = value;

  const result: string[] = [];
  while (content.length > 0) {
    const start = content.indexOf(preTag);
    const end = content.indexOf(postTag);
    if (start >= 0 && end > 0) {
      const item = content.substring(start + preTag.length, end);
      if (!result.includes(item)) {
        result.push(item);
      }
      content = content.substring(end + postTag.length);
    } else {
      break;
    }
  }

  result.sort((a, b) => b.length - a.length);

  return {
    strippedValue,
    replacements: result.map<Replacement>((textToReplace) => ({
      textToReplace,
      replacement: <Wrapper>{textToReplace}</Wrapper>,
      replaceAll: true,
    })),
  };
};

const markContent = (value: string, markerConfig?: MarkerConfig) =>
  markerConfig === undefined ? { strippedValue: value, replacements: [] } : stripAndReplace(value, markerConfig);

const createFallbackContent = (
  value: string,
  LineWrapper?: LineWrapperType,
  replacements: Replacement[] = []
): ReactNode => {
  if (LineWrapper) {
    return value.split("\n").map((line, i) => (
      <LineWrapper key={i} lineNumber={i + 1}>
        {replacements.length ? (
          <SplitAndReplace key={`fract-${i}`} text={line} replacements={replacements} textWrapper={(s) => s} />
        ) : (
          line
        )}
      </LineWrapper>
    ));
  }
  return <SplitAndReplace key="fract" text={value} replacements={replacements} textWrapper={(s) => s} />;
};

const SyntaxHighlighter = ({ value, lineWrapper, language = "text", markerConfig }: Props) => {
  // TODO error
  const { strippedValue, replacements } = markContent(value, markerConfig);
  const { isLoading, tree } = useSyntaxHighlighting({
    value: strippedValue,
    language,
    nodeLimit: 1000,
    groupByLine: !!lineWrapper,
    markerBounds: markerConfig ? { start: markerConfig.start, end: markerConfig.end } : undefined,
  });
  const fallbackContent = useMemo(
    () => createFallbackContent(strippedValue, lineWrapper, replacements),
    [value, lineWrapper, markerConfig]
  );

  if (isLoading || !tree) {
    return (
      <pre>
        <code className={`language-${language}`}>{fallbackContent}</code>
      </pre>
    );
  }

  return (
    <pre>
      <code className={`language-${language}`}>{tree?.map(mapWithDepth(0, lineWrapper, markerConfig?.wrapper))}</code>
    </pre>
  );
};

export default SyntaxHighlighter;
