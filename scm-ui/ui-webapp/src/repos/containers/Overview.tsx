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
import React, { FC, useState } from "react";
import { useHistory, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  CreateButton,
  devices,
  LinkPaginator,
  Notification,
  OverviewPageActions,
  Page,
  PageActions,
  urls
} from "@scm-manager/ui-components";
import RepositoryList from "../components/list";
import { useNamespaces, useRepositories } from "@scm-manager/ui-api";
import { NamespaceCollection, RepositoryCollection } from "@scm-manager/ui-types";
import { ExtensionPoint, extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import styled from "styled-components";

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

  const request = {
    namespace: namespaces?._embedded.namespaces.find(n => n.namespace === namespace),
    // ui starts counting by 1,
    // but backend starts counting by 0
    page: page - 1,
    search,
    // if a namespaces is selected we have to wait
    // until the list of namespaces are loaded from the server
    // also do not fetch repositories if an invalid namespace is selected
    disabled:
      (!!namespace && !namespaces) ||
      (!!namespace && !namespaces?._embedded.namespaces.some(n => n.namespace === namespace))
  };
  const { isLoading: isLoadingRepositories, error: errorRepositories, data: repositories } = useRepositories(request);

  return {
    isLoading: isLoadingNamespaces || isLoadingRepositories,
    error: errorNamespaces || errorRepositories || undefined,
    namespaces,
    namespace,
    repositories,
    search,
    page
  };
};

type RepositoriesProps = {
  namespaces?: NamespaceCollection;
  repositories?: RepositoryCollection;
  search: string;
  page: number;
  namespace?: string;
};

const Repositories: FC<RepositoriesProps> = ({ namespaces, namespace, repositories, search, page }) => {
  const [t] = useTranslation("repos");
  if (namespaces && repositories) {
    if (repositories._embedded && repositories._embedded.repositories.length > 0) {
      return (
        <>
          <RepositoryList
            repositories={repositories._embedded.repositories}
            namespaces={namespaces}
            page={page}
            search={search}
            namespace={namespace}
          />
          <LinkPaginator collection={repositories} page={page} filter={search} />
        </>
      );
    } else {
      return <Notification type="info">{t("overview.noRepositories")}</Notification>;
    }
  } else {
    return <Notification type="info">{t("overview.invalidNamespace")}</Notification>;
  }
};

const Overview: FC = () => {
  const { isLoading, error, namespace, namespaces, repositories, search, page } = useOverviewData();
  const history = useHistory();
  const [t] = useTranslation("repos");
  const binder = useBinder();

  const extensions = binder.getExtensions<extensionPoints.RepositoryOverviewLeft>("repository.overview.left");

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
    namespacesToRender = [allNamespacesPlaceholder, ...namespaces._embedded.namespaces.map(n => n.namespace).sort()];
  }
  const namespaceSelected = (newNamespace: string) => {
    if (newNamespace === allNamespacesPlaceholder) {
      history.push("/repos/");
    } else {
      history.push(`/repos/${newNamespace}/`);
    }
  };

  const hasExtensions = extensions.length > 0;

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
      loading={isLoading}
      error={error}
    >
      <div className="columns">
        {hasExtensions ? (
          <StickyColumn className="column is-one-third">
            {extensions.map(extension => React.createElement(extension))}
          </StickyColumn>
        ) : null}
        <div className="column is-clipped">
          <Repositories
            namespaces={namespaces}
            namespace={namespace}
            repositories={repositories}
            search={search}
            page={page}
          />
          {showCreateButton ? <CreateButton label={t("overview.createButton")} link="/repos/create/" /> : null}
        </div>
      </div>
      <PageActions>
        {showActions ? (
          <OverviewPageActions
            showCreateButton={showCreateButton}
            currentGroup={
              namespace && namespaces?._embedded.namespaces.some(n => n.namespace === namespace) ? namespace : ""
            }
            groups={namespacesToRender}
            groupSelected={namespaceSelected}
            link={namespace ? `repos/${namespace}` : "repos"}
            createLink="/repos/create/"
            label={t("overview.createButton")}
            testId="repository-overview"
            searchPlaceholder={t("overview.filterRepositories")}
          />
        ) : null}
      </PageActions>
    </Page>
  );
};

export default Overview;
