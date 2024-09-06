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

import React, { ComponentType, FC, ReactChild, ReactNode, useMemo } from "react";
import useSyntaxHighlighting from "./useSyntaxHighlighting";
import type { RefractorElement } from "refractor";
import type { Element } from "hast";
import type { MarkerBounds, RefractorNode } from "./types";
import { SplitAndReplace } from "@scm-manager/ui-text";

type LineWrapperType = ComponentType<{ lineNumber: number }>;
type MarkerConfig = MarkerBounds & {
  wrapper: ComponentType;
};
type RendererType = ComponentType<{ children: ReactChild[] }>;

type Replacement = {
  textToReplace: string;
  replacement: ReactNode;
  replaceAll: boolean;
};

const DEFAULT_RENDERER = React.Fragment;
const DEFAULT_LINE_WRAPPER: LineWrapperType = ({ children }) => <>{children}</>;

export type Props = {
  value: string;
  language?: string;
  lineWrapper?: LineWrapperType;
  markerConfig?: MarkerConfig;
  nodeLimit?: number;
  renderer?: RendererType;
  as?: ComponentType;
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
): ReactChild {
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
            <MarkerReplacement key={`marker-${depth}-${i}`}>
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
  lineWrapper?: LineWrapperType,
  renderer?: RendererType,
  replacements: Replacement[] = []
): ReactNode => {
  if (lineWrapper || renderer) {
    const Renderer = renderer ?? DEFAULT_RENDERER;
    const LineWrapper = lineWrapper ?? DEFAULT_LINE_WRAPPER;
    return (
      <Renderer>
        {value.split("\n").map((line, i) => (
          <LineWrapper key={i} lineNumber={i + 1}>
            {replacements.length ? (
              <SplitAndReplace key={`fract-${i}`} text={line} replacements={replacements} textWrapper={(s) => s} />
            ) : (
              line
            )}
          </LineWrapper>
        ))}
      </Renderer>
    );
  }
  return <SplitAndReplace key="fract" text={value} replacements={replacements} textWrapper={(s) => s} />;
};

const useMarkedContent = (value: string, markerConfig?: MarkerConfig) => {
  return useMemo(() => {
    const markedContent = markContent(value, markerConfig);
    return {
      ...markedContent,
      markedTexts: markedContent.replacements.length
        ? markedContent.replacements.map((replacement) => replacement.textToReplace)
        : undefined,
    };
  }, [value, markerConfig]);
};

const useFallbackContent = (
  strippedValue: string,
  replacements: Replacement[],
  lineWrapper?: LineWrapperType,
  renderer?: RendererType
) => {
  return useMemo(
    () => createFallbackContent(strippedValue, lineWrapper, renderer, replacements),
    [strippedValue, lineWrapper, renderer, replacements]
  );
};

type RootWrapperProps = {
  language: string;
  as?: ComponentType;
};

const RootWrapper: FC<RootWrapperProps> = ({ language, children, as: AsComponent }) => {
  if (!AsComponent) {
    return (
      <pre className={language ? `language-${language}` : ""}>
        <code>{children}</code>
      </pre>
    );
  }

  return <AsComponent>{children}</AsComponent>;
};

const SyntaxHighlighter = ({
  value,
  lineWrapper,
  language = "text",
  markerConfig,
  nodeLimit = 10000,
  renderer,
  as,
}: Props) => {
  const { strippedValue, replacements, markedTexts } = useMarkedContent(value, markerConfig);
  const { isLoading, tree, error } = useSyntaxHighlighting({
    value: strippedValue,
    language,
    nodeLimit,
    groupByLine: !!lineWrapper || !!renderer,
    markedTexts: markedTexts,
  });
  const fallbackContent = useFallbackContent(strippedValue, replacements, lineWrapper, renderer);
  const Renderer = renderer ?? DEFAULT_RENDERER;

  // we do not expose the error for now, because we have no idea how to display it
  if (isLoading || !tree || error) {
    return (
      <RootWrapper as={as} language={language}>
        <code>{fallbackContent}</code>
      </RootWrapper>
    );
  }

  return (
    <RootWrapper as={as} language={language}>
      <Renderer>{tree?.map(mapWithDepth(0, lineWrapper, markerConfig?.wrapper))}</Renderer>
    </RootWrapper>
  );
};

export default SyntaxHighlighter;
