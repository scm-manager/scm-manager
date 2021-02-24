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
import React, { ReactNode } from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import { urls } from "@scm-manager/ui-api";

/**
 * Adds anchor links to markdown headings.
 *
 * @see <a href="https://github.com/rexxars/react-markdown/issues/69">Headings are missing anchors / ids</a>
 */

type Props = RouteComponentProps & {
  children: ReactNode;
  level: number;
};

function flatten(text: string, child: any): any {
  return typeof child === "string" ? text + child : React.Children.toArray(child.props.children).reduce(flatten, text);
}

/**
 * Turns heading text into a anchor id
 *
 * @VisibleForTesting
 */
export function headingToAnchorId(heading: string) {
  return heading.toLowerCase().replace(/\W/g, "-");
}

function MarkdownHeadingRenderer(props: Props) {
  const children = React.Children.toArray(props.children);
  const heading = children.reduce(flatten, "");
  const anchorId = headingToAnchorId(heading);
  const headingElement = React.createElement("h" + props.level, {}, props.children);
  const href = urls.withContextPath(props.location.pathname + "#" + anchorId);

  return (
    <a id={`${anchorId}`} className="anchor" href={href}>
      {headingElement}
    </a>
  );
}

export default withRouter(MarkdownHeadingRenderer);
