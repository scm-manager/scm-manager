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
import React from "react";
import { Redirect, Route, Link as RouteLink, Switch, useRouteMatch, match } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { Changeset, Link } from "@scm-manager/ui-types";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  FileControlFactory,
  JumpToFileButton,
  Loading,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation,
  Tooltip,
  urls
} from "@scm-manager/ui-components";
import RepositoryDetails from "../components/RepositoryDetails";
import EditRepo from "./EditRepo";
import BranchesOverview from "../branches/containers/BranchesOverview";
import CreateBranch from "../branches/containers/CreateBranch";
import Permissions from "../permissions/containers/Permissions";
import EditRepoNavLink from "../components/EditRepoNavLink";
import BranchRoot from "../branches/containers/BranchRoot";
import PermissionsNavLink from "../components/PermissionsNavLink";
import RepositoryNavLink from "../components/RepositoryNavLink";
import CodeOverview from "../codeSection/containers/CodeOverview";
import ChangesetView from "./ChangesetView";
import SourceExtensions from "../sources/containers/SourceExtensions";
import TagsOverview from "../tags/container/TagsOverview";
import TagRoot from "../tags/container/TagRoot";
import styled from "styled-components";
import { useIndexLinks, useRepository } from "@scm-manager/ui-api";

const RepositoryTag = styled.span`
  margin-left: 0.2rem;
  background-color: #9a9a9a;
  padding: 0.4rem;
  border-radius: 5px;
  color: white;
  font-weight: bold;
`;

type UrlParams = {
  namespace: string;
  name: string;
};

const useRepositoryFromUrl = (match: match<UrlParams>) => {
  const { namespace, name } = match.params;
  const { data: repository, ...rest } = useRepository(namespace, name);
  return {
    repository,
    ...rest
  };
};

const RepositoryRoot = () => {
  const match = useRouteMatch<UrlParams>();
  const { isLoading, error, repository } = useRepositoryFromUrl(match);
  const indexLinks = useIndexLinks();

  const [t] = useTranslation("repos");

  if (error) {
    return (
      <ErrorPage title={t("repositoryRoot.errorTitle")} subtitle={t("repositoryRoot.errorSubtitle")} error={error} />
    );
  }

  if (!repository || isLoading) {
    return <Loading />;
  }

  const url = urls.matchedUrlFromMatch(match);

  // props used for extensions
  // most of the props required for compatibility
  const props = {
    namespace: repository.namespace,
    name: repository.name,
    repository: repository,
    loading: isLoading,
    error,
    repoLink: (indexLinks.repositories as Link)?.href,
    indexLinks,
    match
  };

  const redirectUrlFactory = binder.getExtension("repository.redirect", props);
  let redirectedUrl;
  if (redirectUrlFactory) {
    redirectedUrl = url + redirectUrlFactory(props);
  } else {
    redirectedUrl = url + "/info";
  }

  const fileControlFactoryFactory: (changeset: Changeset) => FileControlFactory = changeset => file => {
    const baseUrl = `${url}/code/sources`;
    const sourceLink = file.newPath && {
      url: `${baseUrl}/${changeset.id}/${file.newPath}/`,
      label: t("diff.jumpToSource")
    };
    const targetLink = file.oldPath &&
      changeset._embedded?.parents?.length === 1 && {
        url: `${baseUrl}/${changeset._embedded.parents[0].id}/${file.oldPath}`,
        label: t("diff.jumpToTarget")
      };

    const links = [];
    switch (file.type) {
      case "add":
        if (sourceLink) {
          links.push(sourceLink);
        }
        break;
      case "delete":
        if (targetLink) {
          links.push(targetLink);
        }
        break;
      default:
        if (targetLink && sourceLink) {
          links.push(targetLink, sourceLink); // Target link first because its the previous file
        } else if (sourceLink) {
          links.push(sourceLink);
        }
    }

    return links ? links.map(({ url, label }) => <JumpToFileButton tooltip={label} link={url} />) : null;
  };

  const repositoryFlags = [];
  if (repository.archived) {
    repositoryFlags.push(
      <Tooltip message={t("archive.tooltip")}>
        <RepositoryTag className="is-size-6">{t("repository.archived")}</RepositoryTag>
      </Tooltip>
    );
  }

  if (repository.exporting) {
    repositoryFlags.push(
      <Tooltip message={t("exporting.tooltip")}>
        <RepositoryTag className="is-size-6">{t("repository.exporting")}</RepositoryTag>
      </Tooltip>
    );
  }

  const titleComponent = (
    <>
      <RouteLink to={`/repos/${repository.namespace}/`} className={"has-text-dark"}>
        {repository.namespace}
      </RouteLink>
      /{repository.name}
    </>
  );

  const extensionProps = {
    repository,
    url,
    indexLinks
  };

  const matchesBranches = (route: any) => {
    const regex = new RegExp(`${url}/branch/.+/info`);
    return route.location.pathname.match(regex);
  };

  const matchesTags = (route: any) => {
    const regex = new RegExp(`${url}/tag/.+/info`);
    return route.location.pathname.match(regex);
  };

  const matchesCode = (route: any) => {
    const regex = new RegExp(`${url}(/code)/.*`);
    return route.location.pathname.match(regex);
  };

  const getCodeLinkname = () => {
    if (repository?._links?.sources) {
      return "sources";
    }
    if (repository?._links?.changesets) {
      return "changesets";
    }
    return "";
  };

  const evaluateDestinationForCodeLink = () => {
    if (repository?._links?.sources) {
      return `${url}/code/sources/`;
    }
    return `${url}/code/changesets`;
  };

  return (
    <StateMenuContextProvider>
      <Page
        title={titleComponent}
        documentTitle={`${repository.namespace}/${repository.name}`}
        afterTitle={
          <>
            <ExtensionPoint name={"repository.afterTitle"} props={{ repository }} />
            {repositoryFlags.map(flag => flag)}
          </>
        }
      >
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Switch>
              <Redirect exact from={match.url} to={redirectedUrl} />

              {/* redirect pre 2.0.0-rc2 links */}
              <Redirect from={`${url}/changeset/:id`} to={`${url}/code/changeset/:id`} />
              <Redirect exact from={`${url}/sources`} to={`${url}/code/sources`} />
              <Redirect from={`${url}/sources/:revision/:path*`} to={`${url}/code/sources/:revision/:path*`} />
              <Redirect exact from={`${url}/changesets`} to={`${url}/code/changesets`} />
              <Redirect from={`${url}/branch/:branch/changesets`} to={`${url}/code/branch/:branch/changesets/`} />

              <Route path={`${url}/info`} exact component={() => <RepositoryDetails repository={repository} />} />
              <Route path={`${url}/settings/general`}>
                <EditRepo repository={repository} />
              </Route>
              <Route path={`${url}/settings/permissions`}>
                <Permissions namespaceOrRepository={repository} />
              </Route>
              <Route
                exact
                path={`${url}/code/changeset/:id`}
                render={() => (
                  <ChangesetView repository={repository} fileControlFactoryFactory={fileControlFactoryFactory} />
                )}
              />
              <Route
                path={`${url}/code/sourceext/:extension`}
                exact={true}
                render={() => <SourceExtensions repository={repository} />}
              />
              <Route
                path={`${url}/code/sourceext/:extension/:revision/:path*`}
                render={() => <SourceExtensions repository={repository} baseUrl={`${url}/code/sources`} />}
              />
              <Route path={`${url}/code`}>
                <CodeOverview baseUrl={`${url}/code`} repository={repository} />
              </Route>
              <Route
                path={`${url}/branch/:branch`}
                render={() => <BranchRoot repository={repository} baseUrl={`${url}/branch`} />}
              />
              <Route
                path={`${url}/branches`}
                exact={true}
                render={() => <BranchesOverview repository={repository} baseUrl={`${url}/branch`} />}
              />
              <Route path={`${url}/branches/create`} render={() => <CreateBranch repository={repository} />} />
              <Route
                path={`${url}/tag/:tag`}
                render={() => <TagRoot repository={repository} baseUrl={`${url}/tag`} />}
              />
              <Route
                path={`${url}/tags`}
                exact={true}
                render={() => <TagsOverview repository={repository} baseUrl={`${url}/tag`} />}
              />
              <ExtensionPoint name="repository.route" props={extensionProps} renderAll={true} />
            </Switch>
          </PrimaryContentColumn>
          <SecondaryNavigationColumn>
            <SecondaryNavigation label={t("repositoryRoot.menu.navigationLabel")}>
              <ExtensionPoint name="repository.navigation.topLevel" props={extensionProps} renderAll={true} />
              <NavLink
                to={`${url}/info`}
                icon="fas fa-info-circle"
                label={t("repositoryRoot.menu.informationNavLink")}
                title={t("repositoryRoot.menu.informationNavLink")}
              />
              <RepositoryNavLink
                repository={repository}
                linkName="branches"
                to={`${url}/branches/`}
                icon="fas fa-code-branch"
                label={t("repositoryRoot.menu.branchesNavLink")}
                activeWhenMatch={matchesBranches}
                activeOnlyWhenExact={false}
                title={t("repositoryRoot.menu.branchesNavLink")}
              />
              <RepositoryNavLink
                repository={repository}
                linkName="tags"
                to={`${url}/tags/`}
                icon="fas fa-tags"
                label={t("repositoryRoot.menu.tagsNavLink")}
                activeWhenMatch={matchesTags}
                activeOnlyWhenExact={false}
                title={t("repositoryRoot.menu.tagsNavLink")}
              />
              <RepositoryNavLink
                repository={repository}
                linkName={getCodeLinkname()}
                to={evaluateDestinationForCodeLink()}
                icon="fas fa-code"
                label={t("repositoryRoot.menu.sourcesNavLink")}
                activeWhenMatch={matchesCode}
                activeOnlyWhenExact={false}
                title={t("repositoryRoot.menu.sourcesNavLink")}
              />
              <ExtensionPoint name="repository.navigation" props={extensionProps} renderAll={true} />
              <SubNavigation
                to={`${url}/settings/general`}
                label={t("repositoryRoot.menu.settingsNavLink")}
                title={t("repositoryRoot.menu.settingsNavLink")}
              >
                <EditRepoNavLink repository={repository} editUrl={`${url}/settings/general`} />
                <PermissionsNavLink permissionUrl={`${url}/settings/permissions`} repository={repository} />
                <ExtensionPoint name="repository.setting" props={extensionProps} renderAll={true} />
              </SubNavigation>
            </SecondaryNavigation>
          </SecondaryNavigationColumn>
        </CustomQueryFlexWrappedColumns>
      </Page>
    </StateMenuContextProvider>
  );
};

export default RepositoryRoot;
