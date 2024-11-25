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
        <h3 className="has-text-weight-bold">
          <span className="is-sr-only" lang="en">
            {t("repositoryOverview.namespace")}
          </span>
          {group.name}
        </h3>
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
