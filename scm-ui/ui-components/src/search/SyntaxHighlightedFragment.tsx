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

import React, { FC, ReactNode, useEffect, useMemo, useState } from "react";
import createAdapter from "../repos/refractorAdapter";
// @ts-ignore no types for css modules
import theme from "../syntax-highlighting.module.css";
import SplitAndReplace, { Replacement } from "../SplitAndReplace";
import { AST, RefractorNode } from "refractor";
import { determineLanguage } from "../languages";

const PRE_TAG = "<|[[--";
const POST_TAG = "--]]|>";
const PRE_TAG_REGEX = /<\|\[\[--/g;
const POST_TAG_REGEX = /--]]\|>/g;

const adapter = createAdapter(theme);

function createReplacement(textToReplace: string): Replacement {
  return {
    textToReplace,
    replacement: <mark>{textToReplace}</mark>,
    replaceAll: true
  };
}

function mapWithDepth(depth: number, replacements: Replacement[]) {
  return function mapChildrenWithDepth(child: RefractorNode, i: number) {
    return mapChild(child, i, depth, replacements);
  };
}

function isAstElement(node: RefractorNode): node is AST.Element {
  return (node as AST.Element).tagName !== undefined;
}

function mapChild(child: RefractorNode, i: number, depth: number, replacements: Replacement[]): ReactNode {
  if (isAstElement(child)) {
    const className =
      child.properties && Array.isArray(child.properties.className)
        ? child.properties.className.join(" ")
        : child.properties.className;

    return React.createElement(
      child.tagName,
      Object.assign({ key: `fract-${depth}-${i}` }, child.properties, { className }),
      child.children && child.children.map(mapWithDepth(depth + 1, replacements))
    );
  }

  return (
    <SplitAndReplace key={`fract-${depth}-${i}`} text={child.value} replacements={replacements} textWrapper={s => s} />
  );
}

type Props = {
  value: string;
  language: string;
};

const stripAndReplace = (value: string) => {
  const strippedValue = value.replace(PRE_TAG_REGEX, "").replace(POST_TAG_REGEX, "");
  let content = value;

  const result: string[] = [];
  while (content.length > 0) {
    const start = content.indexOf(PRE_TAG);
    const end = content.indexOf(POST_TAG);
    if (start >= 0 && end > 0) {
      const item = content.substring(start + PRE_TAG.length, end);
      if (!result.includes(item)) {
        result.push(item);
      }
      content = content.substring(end + POST_TAG.length);
    } else {
      break;
    }
  }

  result.sort((a, b) => b.length - a.length);

  return {
    strippedValue,
    replacements: result.map(createReplacement)
  };
};

const SyntaxHighlightedFragment: FC<Props> = ({ value, language }) => {
  const [isLoading, setIsLoading] = useState(true);
  const determinedLanguage = determineLanguage(language);
  const { strippedValue, replacements } = useMemo(() => stripAndReplace(value), [value]);

  useEffect(() => {
    adapter.loadLanguage(determinedLanguage, () => {
      setIsLoading(false);
    });
  }, [determinedLanguage]);

  if (isLoading) {
    return <SplitAndReplace text={strippedValue} replacements={replacements} textWrapper={s => s} />;
  }

  const refractorNodes = adapter.highlight(strippedValue, determinedLanguage);
  const highlightedFragment = refractorNodes.map(mapWithDepth(0, replacements));

  return <>{highlightedFragment}</>;
};

export default SyntaxHighlightedFragment;
