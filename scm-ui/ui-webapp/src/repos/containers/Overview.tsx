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

import React, { FC, useEffect, useState } from "react";
import { useHistory, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  CreateButton,
  devices,
  ErrorNotification,
  LinkPaginator,
  Loading,
  Notification,
  OverviewPageActions,
  Page,
  PageActions,
  urls,
} from "@scm-manager/ui-components";
import { useDocumentTitle } from "@scm-manager/ui-core";
import RepositoryList from "../components/list";
import { useNamespaceAndNameContext, useNamespaces, useRepositories } from "@scm-manager/ui-api";
import { NamespaceCollection, RepositoryCollection } from "@scm-manager/ui-types";
import { binder, ExtensionPoint, extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import styled from "styled-components";
import { KeyboardIterator, KeyboardSubIterator } from "@scm-manager/ui-shortcuts";
import classNames from "classnames";

const StickyColumn = styled.div`
  align-self: flex-start;

  &:empty {
    display: none;
  }

  @media (min-width: ${devices.mobile.width}px) {
    position: sticky;
    top: 1rem;
  }
`;

const useUrlParams = () => {
  const params = useParams();
  return urls.getNamespaceAndPageFromMatch({ params });
};

const useOverviewData = () => {
  const { namespace, page } = useUrlParams();
  const { isLoading: isLoadingNamespaces, error: errorNamespaces, data: namespaces } = useNamespaces();
  const location = useLocation();
  const search = urls.getQueryStringFromLocation(location);

  const listOptions = binder.getExtension<extensionPoints.RepositoryOverviewListOptionsExtensionPoint>(
    "repository.overview.listOptions"
  );

  const listOptionsValue = listOptions ? listOptions() : { pageSize: 10, showArchived: true };

  const request = {
    namespace: namespaces?._embedded.namespaces.find((n) => n.namespace === namespace),
    // ui starts counting by 1,
    // but backend starts counting by 0
    page: page - 1,
    search,
    ...listOptionsValue,
    // if a namespaces is selected we have to wait
    // until the list of namespaces are loaded from the server
    // also do not fetch repositories if an invalid namespace is selected
    disabled:
      (!!namespace && !namespaces) ||
      (!!namespace && !namespaces?._embedded.namespaces.some((n) => n.namespace === namespace)),
  };
  const { isLoading: isLoadingRepositories, error: errorRepositories, data: repositories } = useRepositories(request);

  return {
    isLoading: isLoadingNamespaces || isLoadingRepositories,
    error: errorNamespaces || errorRepositories || undefined,
    namespaces,
    namespace,
    repositories,
    search,
    page,
  };
};

type RepositoriesProps = {
  namespaces?: NamespaceCollection;
  repositories?: RepositoryCollection;
  search?: string;
  page: number;
  isLoading?: boolean;
  error?: Error;
  hasTopExtension?: boolean;
};

const Repositories: FC<RepositoriesProps> = ({
  namespaces,
  repositories,
  hasTopExtension,
  search,
  page,
  error,
  isLoading,
}) => {
  const [t] = useTranslation("repos");
  let header;
  if (hasTopExtension) {
    header = (
      <div className={classNames("is-flex", "is-align-items-center", "is-size-6", "has-text-weight-bold", "p-3")}>
        {t("overview.title")}
      </div>
    );
  }
  if (error) {
    return (
      <>
        {header}
        <ErrorNotification error={error} />
      </>
    );
  } else if (isLoading) {
    return (
      <>
        {header}
        <Loading />
      </>
    );
  } else if (namespaces && repositories) {
    if (repositories._embedded && repositories._embedded.repositories.length > 0) {
      return (
        <>
          <RepositoryList repositories={repositories._embedded.repositories} namespaces={namespaces} />
          <LinkPaginator collection={repositories} page={page} filter={search} />
        </>
      );
    } else {
      return (
        <>
          {header}
          <Notification type="info">{t("overview.noRepositories")}</Notification>
        </>
      );
    }
  } else {
    return (
      <>
        {header}
        <Notification type="info">{t("overview.invalidNamespace")}</Notification>
      </>
    );
  }
};

function getCurrentGroup(namespace?: string, namespaces?: NamespaceCollection) {
  return namespace && namespaces?._embedded.namespaces.some((n) => n.namespace === namespace) ? namespace : "";
}

const Overview: FC = () => {
  const { isLoading, error, namespace, namespaces, repositories, search, page } = useOverviewData();
  const history = useHistory();
  const [t] = useTranslation("repos");
  const getDocumentTitle = () => {
    if (repositories?.pageTotal && repositories.pageTotal > 1 && page) {
      if (namespace) {
        return t("overview.titleWithNamespaceAndPage", { page, total: repositories.pageTotal, namespace });
      } else {
        return t("overview.titleWithPage", { page, total: repositories.pageTotal });
      }
    } else if (namespace) {
      return t("overview.titleWithNamespace", { namespace });
    } else {
      return t("overview.title");
    }
  };
  useDocumentTitle(getDocumentTitle());
  const binder = useBinder();
  const context = useNamespaceAndNameContext();
  useEffect(() => {
    context.setNamespace(namespace || "");
    return () => {
      context.setNamespace("");
    };
  }, [namespace, context]);

  // we keep the create permission in the state,
  // because it does not change during searching or paging
  // and we can avoid bouncing of search bar elements
  const [showCreateButton, setShowCreateButton] = useState(false);
  if (!showCreateButton && !!repositories?._links.create) {
    setShowCreateButton(true);
  }

  // We need to keep track if we have already load the site,
  // because we only know if we can create repositories and
  // we need this information to show the create button.
  // In order to avoid bouncing of the ui elements we want to
  // wait until we have all information before we show the top
  // action bar.
  const [showActions, setShowActions] = useState(false);
  if (!showActions && repositories) {
    setShowActions(true);
  }

  const allNamespacesPlaceholder = t("overview.allNamespaces");
  let namespacesToRender: string[] = [];
  if (namespaces) {
    namespacesToRender = [allNamespacesPlaceholder, ...namespaces._embedded.namespaces.map((n) => n.namespace).sort()];
  }
  const namespaceSelected = (newNamespace: string) => {
    if (newNamespace === allNamespacesPlaceholder) {
      history.push("/repos/");
    } else {
      history.push(`/repos/${newNamespace}/`);
    }
  };

  const createLink = namespace ? `/repos/create/?namespace=${namespace}` : "/repos/create/";
  return (
    <Page
      documentTitle={t("overview.title")}
      title={
        <ExtensionPoint<extensionPoints.RepositoryOverviewTitle> name="repository.overview.title">
          {t("overview.title")}
        </ExtensionPoint>
      }
      subtitle={
        <ExtensionPoint<extensionPoints.RepositoryOverviewSubtitle> name="repository.overview.subtitle">
          {t("overview.subtitle")}
        </ExtensionPoint>
      }
    >
      <div className="columns">
        {binder.hasExtension<extensionPoints.RepositoryOverviewLeft>("repository.overview.left") ? (
          <StickyColumn className="column is-one-third">
            {<ExtensionPoint<extensionPoints.RepositoryOverviewLeft> name="repository.overview.left" renderAll />}
          </StickyColumn>
        ) : null}
        <div className="column is-clipped">
          <KeyboardIterator>
            <KeyboardSubIterator>
              {binder.hasExtension<extensionPoints.RepositoryOverviewTop>("repository.overview.top", {
                page,
                search,
                namespace,
              }) ? (
                <ExtensionPoint<extensionPoints.RepositoryOverviewTop>
                  name="repository.overview.top"
                  renderAll={true}
                  props={{
                    page,
                    search,
                    namespace,
                  }}
                />
              ) : null}
            </KeyboardSubIterator>
            <Repositories
              namespaces={namespaces}
              repositories={repositories}
              search={search}
              page={page}
              isLoading={isLoading}
              error={error}
              hasTopExtension={binder.hasExtension<extensionPoints.RepositoryOverviewTop>("repository.overview.top", {
                page,
                search,
                namespace,
              })}
            />
          </KeyboardIterator>
          {showCreateButton ? <CreateButton label={t("overview.createButton")} link={createLink} /> : null}
        </div>
      </div>
      <PageActions>
        {showActions ? (
          <>
            <label id="select-namespace" hidden>
              {t("overview.filterByNamespace")}
            </label>
            <OverviewPageActions
              showCreateButton={showCreateButton}
              currentGroup={getCurrentGroup(namespace, namespaces)}
              groups={namespacesToRender}
              groupSelected={namespaceSelected}
              groupAriaLabelledby="select-namespace"
              link={namespace ? `repos/${namespace}` : "repos"}
              createLink={createLink}
              label={t("overview.createButton")}
              testId="repository-overview"
              searchPlaceholder={t("overview.filterRepositories")}
            />
          </>
        ) : null}
      </PageActions>
    </Page>
  );
};

export default Overview;
