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

import React, { FC, ReactNode, useState } from "react";
import { useHistory, useLocation } from "react-router-dom";
import { urls } from "@scm-manager/ui-api";
import styled from "styled-components";
import Icon from "../Icon";
import Tooltip from "../Tooltip";
import { useTranslation } from "react-i18next";
import copyToClipboard from "../CopyToClipboard";

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
  id?: string;
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

const MarkdownHeadingRenderer: FC<Props> = ({ children, level, permalink, id }) => {
  const [copying, setCopying] = useState(false);
  const [t] = useTranslation("repos");
  const location = useLocation();
  const history = useHistory();
  const reactChildren = React.Children.toArray(children);
  const heading = reactChildren.reduce(flatten, "");
  const anchorId = id || headingToAnchorId(heading);
  const copyPermalink = (event: React.MouseEvent) => {
    event.preventDefault();
    setCopying(true);
    copyToClipboard(permalinkHref)
      .then(() => history.replace("#" + anchorId))
      .finally(() => setCopying(false));
  };
  const CopyButton = copying ? (
    <Icon name="spinner fa-spin" alt={t("sources.content.loading")} />
  ) : (
    <Tooltip message={t("sources.content.copyPermalink")}>
      <Icon name="link" onClick={copyPermalink} alt={t("sources.content.copyPermalink")} />
    </Tooltip>
  );
  const headingElement = React.createElement("h" + level, { id: anchorId }, [...reactChildren, CopyButton]);
  const href = urls.withContextPath(location.pathname + "#" + anchorId);
  const permalinkHref =
    window.location.protocol +
    "//" +
    window.location.host +
    urls.withContextPath((permalink || location.pathname) + "#" + anchorId);

  return (
    <Link className="anchor" href={href}>
      {headingElement}
    </Link>
  );
};

export const create = (permalink: string): FC<Props> => {
  return (props) => <MarkdownHeadingRenderer {...props} permalink={permalink} />;
};

export default MarkdownHeadingRenderer;
