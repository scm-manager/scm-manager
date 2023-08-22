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
import React, { FC, ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { RepositoryGroup } from "@scm-manager/ui-types";
import { useLocalStorage } from "@scm-manager/ui-api";
import Icon from "../Icon";

const Separator = styled.div`
  border-bottom: 1px solid rgb(219, 219, 219, 0.5);
`;

type Props = {
  group: RepositoryGroup;
  elements: ReactNode[];
};

const GroupEntries: FC<Props> = ({ group, elements }) => {
  const [t] = useTranslation("namespaces");
  const [collapsed, setCollapsed] = useLocalStorage<boolean | null>(`repoNamespace.${group.name}.collapsed`, null);

  const content = elements.map((entry, index) => (
    <React.Fragment key={index}>
      <div>{entry}</div>
      {index + 1 !== elements.length ? <Separator className="mx-4" /> : null}
    </React.Fragment>
  ));

  const settingsLink = group.namespace?._links?.permissions && (
    <Link to={`/namespace/${group.name}/settings`} aria-label={t("repositoryOverview.settings.tooltip")}>
      <Icon
        color="inherit"
        name="cog"
        title={t("repositoryOverview.settings.tooltip")}
        className="is-size-6 ml-2 has-text-link"
      />
    </Link>
  );

  return (
    <>
      <div
        className={classNames(
          "is-flex",
          "is-align-items-center",
          "is-justify-content-space-between",
          "is-size-6",
          "has-text-weight-bold",
          "p-3",
          "has-cursor-pointer"
        )}
        onClick={() => setCollapsed(!collapsed)}
      >
        <span>
          <Link to={`/repos/${group.name}/`} className="has-text-inherit">
            {group.name}
          </Link>{" "}
          {settingsLink}
        </span>
        <Icon
          color="inherit"
          name={collapsed ? "caret-left" : "caret-down"}
          title={t("repositoryOverview.settings.tooltip")}
          className="is-size-6 ml-2"
        />
      </div>
      {collapsed ? null : <div className={classNames("box", "p-2")}>{content}</div>}
      <div className="is-clearfix" />
    </>
  );
};

export default GroupEntries;
