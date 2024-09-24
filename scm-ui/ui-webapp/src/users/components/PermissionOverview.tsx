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

import React, { FC, useState } from "react";
import { Checkbox, ErrorNotification, Icon, Loading, Notification } from "@scm-manager/ui-components";
import {
  Group,
  Link as HalLink,
  Links,
  Namespace,
  PermissionOverview as Data,
  PermissionOverviewGroupEntry,
  Repository,
  User,
} from "@scm-manager/ui-types";
import styled from "styled-components";
import { useUserPermissionOverview } from "@scm-manager/ui-api";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

const NamespaceColumn = styled.th`
  width: 1rem;
`;

const EditIcon: FC = () => {
  const [t] = useTranslation("users");
  return <Icon alt={t("permissionOverview.edit")} name="pen" />;
};

const EditLink: FC<{ links?: Links; to: string }> = ({ links, to }) => {
  if (!links?.permissions) {
    return null;
  }
  return (
    <Link to={to}>
      <EditIcon />
    </Link>
  );
};

const ElementLink: FC<{ link?: HalLink; to: string }> = ({ link, to, children }) => {
  if (!link) {
    return <>{children}</>;
  }
  return <Link to={to}>{children}</Link>;
};

const GroupRow: FC<{ entry: PermissionOverviewGroupEntry; group?: Group }> = ({ entry, group }) => (
  <tr>
    <td>
      <ElementLink to={`/group/${entry.name}/`} link={group?._links?.self as HalLink}>
        {entry.name}
      </ElementLink>
    </td>
    <td align="center">{entry.permissions && <Icon name="check" />}</td>
    <td align="center">
      <EditLink to={`/group/${entry.name}/settings/permissions`} links={group?._links} />
    </td>
  </tr>
);

const NotCreatedGroupRow: FC<{ entry: PermissionOverviewGroupEntry }> = ({ entry }) => (
  <tr>
    <td>{entry.name}</td>
    <td align="center">
      <Link to={`/groups/create/?name=${entry.name}&external=true`}>
        <EditIcon />
      </Link>
    </td>
  </tr>
);

const RepositoryNamespaceRows: FC<{
  entry: { namespace: Namespace; repositories: Repository[] };
  relevant: boolean;
}> = ({ entry, relevant }) => (
  <>
    <NamespaceRow key={entry.namespace.namespace} namespace={entry.namespace} relevant={relevant} />
    {entry.repositories.map((repository) => (
      <RepositoryRow key={`${repository.namespace}/${repository.name}`} entry={repository} />
    ))}
  </>
);

const NamespaceRow: FC<{ namespace: Namespace; relevant: boolean }> = ({ namespace, relevant }) => (
  <tr>
    <td colSpan={2}>
      <Link to={`/repos/${namespace.namespace}/`}>{namespace.namespace}</Link>
    </td>
    <td align="center">{relevant && <Icon name="check" />}</td>
    <td align="center">
      <EditLink links={namespace._links} to={`/namespace/${namespace.namespace}/settings/permissions`} />
    </td>
  </tr>
);

const RepositoryRow: FC<{ entry: Repository }> = ({ entry }) => (
  <tr>
    <td />
    <td>
      <Link to={`/repo/${entry.namespace}/${entry.name}/`}>{entry.name}</Link>
    </td>
    <td align="center">
      <Icon name="check" />
    </td>
    <td align="center">
      <EditLink links={entry._links} to={`/repo/${entry.namespace}/${entry.name}/settings/permissions`} />
    </td>
  </tr>
);

const GroupTable: FC<{ data: Data }> = ({ data }) => {
  const [t] = useTranslation("users");

  if (data.relevantGroups.find((entry) => !entry.externalOnly)) {
    return (
      <table>
        <thead>
          <tr>
            <th>{t("permissionOverview.groups.groupName")}</th>
            <th align="center">{t("permissionOverview.groups.permissionsConfigured")}</th>
            <th align="center">{t("permissionOverview.groups.editPermissions")}</th>
          </tr>
        </thead>
        <tbody>
          {data.relevantGroups
            .filter((entry) => !entry.externalOnly)
            .map((entry) => (
              <GroupRow
                key={entry.name}
                entry={entry}
                group={(data._embedded?.groups as Group[]).find((group) => group.name === entry.name)}
              />
            ))}
        </tbody>
      </table>
    );
  } else {
    return <Notification type="info">{t("permissionOverview.groups.noGroupsFound")}</Notification>;
  }
};

const GroupsWithoutPermissionTable: FC<{ data: Data }> = ({ data }) => {
  const [t] = useTranslation("users");
  const [external, setExternal] = useState(false);

  let content;
  if (!external) {
    content = null;
  } else if (data.relevantGroups.find((entry) => entry.externalOnly)) {
    content = (
      <table>
        <thead>
          <tr>
            <th>{t("permissionOverview.groups.groupName")}</th>
            <th align="center">{t("permissionOverview.groups.createGroup")}</th>
          </tr>
        </thead>
        <tbody>
          {data.relevantGroups
            .filter((entry) => entry.externalOnly)
            .map((entry) => (
              <NotCreatedGroupRow key={entry.name} entry={entry} />
            ))}
        </tbody>
      </table>
    );
  } else {
    content = <Notification type="info">{t("permissionOverview.groups.noUnknownGroupsFound")}</Notification>;
  }

  return (
    <>
      <Checkbox
        label={t("permissionOverview.groups.showGroupsWithoutPermission")}
        onChange={setExternal}
        helpText={t("permissionOverview.groups.showGroupsWithoutPermissionHelp")}
      />
      {content}
    </>
  );
};

const RepositoryTable: FC<{ data: Data }> = ({ data }) => {
  const [t] = useTranslation("users");

  if ((!data.relevantNamespaces || data.relevantNamespaces.length === 0) && !data.relevantRepositories) {
    return <Notification type="info">{t("permissionOverview.groups.noRepositoriesFound")}</Notification>;
  }

  data.relevantRepositories.sort((r1, r2) =>
    r1.namespace === r2.namespace ? (r1.name < r2.name ? -1 : +1) : r1.namespace < r2.namespace ? -1 : +1
  );

  const findRelevantNamespace = (namespace: string) =>
    (data._embedded?.relevantNamespaces as Namespace[]).find((n: Namespace) => n.namespace === namespace);
  const findOtherNamespace = (namespace: string) =>
    (data._embedded?.otherNamespaces as Namespace[]).find((n: Namespace) => n.namespace === namespace);

  const allNamespaces = new Set<string>();
  data.relevantRepositories.forEach((repo) => allNamespaces.add(repo.namespace));
  data.relevantNamespaces.forEach((namespace) => allNamespaces.add(namespace));
  const sortedNamespaces: string[] = Array.from(allNamespaces).sort();

  const repositoriesForNamespace = (namespace: string) =>
    data.relevantRepositories
      .filter((repo) => repo.namespace === namespace)
      .map(
        (repo) =>
          (data._embedded?.repositories as Repository[]).find(
            (r: Repository) => r.namespace === repo.namespace && r.name === repo.name
          ) || ({ ...repo, _links: {} } as Repository)
      );

  const reposInNamespaces = sortedNamespaces.map((namespace) => {
    return {
      namespace: findRelevantNamespace(namespace) ||
        findOtherNamespace(namespace) || { namespace: namespace, _links: {} },
      repositories: repositoriesForNamespace(namespace),
    };
  });

  return (
    <table>
      <thead>
        <tr>
          <NamespaceColumn colSpan={2}>{t("permissionOverview.repositories.namespaceName")}</NamespaceColumn>
          <th align="center">{t("permissionOverview.repositories.permissionsConfigured")}</th>
          <th align="center">{t("permissionOverview.repositories.editPermissions")}</th>
        </tr>
      </thead>
      <tbody>
        {reposInNamespaces.map((entry) => (
          <RepositoryNamespaceRows
            key={entry.namespace.namespace}
            entry={entry}
            relevant={!!findRelevantNamespace(entry.namespace.namespace)}
          />
        ))}
      </tbody>
    </table>
  );
};

const PermissionOverview: FC<{ user: User }> = ({ user }) => {
  const { data, isLoading, error } = useUserPermissionOverview(user);
  const [t] = useTranslation("users");

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || !data) {
    return <Loading />;
  }

  // To test the table with the "not created" groups, you can mock such data
  // with the following statement and assign this in the GroupsWithoutPermissionTable:
  // const mockedData = {
  //   ...data,
  //   relevantGroups: [...data.relevantGroups, { name: "hitchhiker", permissions: false, externalOnly: true }],
  // };

  return (
    <>
      <GroupTable data={data} />
      <GroupsWithoutPermissionTable data={data} />
      <h4>{t("permissionOverview.repositories.subtitle")}</h4>
      <RepositoryTable data={data} />
    </>
  );
};

export default PermissionOverview;
