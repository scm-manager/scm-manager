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
import React, { FC, ReactNode, useState } from "react";
import { useHistory, useLocation } from "react-router-dom";
import { urls } from "@scm-manager/ui-api";
import styled from "styled-components";
import Icon from "./Icon";
import Tooltip from "./Tooltip";
import { useTranslation } from "react-i18next";
import copyToClipboard from "./CopyToClipboard";

/**
 * Adds anchor links to markdown headings.
 *
 * @see <a href="https://github.com/rexxars/react-markdown/issues/69">Headings are missing anchors / ids</a>
 */

const Link = styled.a`
  i {
    font-size: 1rem;
    visibility: hidden;
    margin-left: 10px;
  }

  i:hover {
    cursor: pointer;
  }

  &:hover i {
    visibility: visible;
  }
`;

type Props = {
  children: ReactNode;
  level: number;
  permalink: string;
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

const MarkdownHeadingRenderer: FC<Props> = ({ children, level, permalink }) => {
  const [copying, setCopying] = useState(false);
  const [t] = useTranslation("repos");
  const location = useLocation();
  const history = useHistory();
  const reactChildren = React.Children.toArray(children);
  const heading = reactChildren.reduce(flatten, "");
  const anchorId = headingToAnchorId(heading);
  const copyPermalink = (event: React.MouseEvent) => {
    event.preventDefault();
    setCopying(true);
    copyToClipboard(permalinkHref)
      .then(() => history.replace("#" + anchorId))
      .finally(() => setCopying(false));
  };
  const CopyButton = copying ? (
    <Icon name="spinner fa-spin" />
  ) : (
    <Tooltip message={t("sources.content.copyPermalink")}>
      <Icon name="link" onClick={copyPermalink} />
    </Tooltip>
  );
  const headingElement = React.createElement("h" + level, {}, [...reactChildren, CopyButton]);
  const href = urls.withContextPath(location.pathname + "#" + anchorId);
  const permalinkHref =
    window.location.protocol +
    "//" +
    window.location.host +
    urls.withContextPath((permalink || location.pathname) + "#" + anchorId);

  return (
    <Link id={`${anchorId}`} className="anchor" href={href}>
      {headingElement}
    </Link>
  );
};

export const create = (permalink: string): FC<Props> => {
  return props => <MarkdownHeadingRenderer {...props} permalink={permalink} />;
};

export default MarkdownHeadingRenderer;
