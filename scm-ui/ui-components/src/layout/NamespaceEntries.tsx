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
import { useTranslation } from "react-i18next";
import { useLocalStorage } from "@scm-manager/ui-api";
import { CardList, Collapsible } from "@scm-manager/ui-layout";
import { RepositoryGroup } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";
import { Icon } from "../index";

type Props = {
  elements: ReactNode[];
  group: RepositoryGroup;
};

const DefaultGroupHeader: FC<{ group: RepositoryGroup }> = ({ group }) => {
  const [t] = useTranslation("namespaces");
  return (
    <>
      <Link to={`/repos/${group.name}/`} className="has-text-inherit">
        <h3 className="has-text-weight-bold">{group.name}</h3>
      </Link>{" "}
      <Link to={`/namespace/${group.name}/info`} aria-label={t("repositoryOverview.settings.tooltip")}>
        <Icon color="inherit" name="cog" title={t("repositoryOverview.settings.tooltip")} className="is-size-6 ml-2" />
      </Link>
    </>
  );
};

const NamespaceEntries: FC<Props> = ({ elements, group }) => {
  const [collapsed, setCollapsed] = useLocalStorage<boolean | null>(`repoNamespace.${group.name}.collapsed`, null);

  return (
    <Collapsible
      collapsed={collapsed ?? false}
      onCollapsedChange={setCollapsed}
      className="mb-5"
      header={<DefaultGroupHeader group={group} />}
    >
      <CardList>{elements}</CardList>
    </Collapsible>
  );
};

export default NamespaceEntries;
