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
import React, { useState } from "react";
import { match as Match } from "react-router";
import { Link as RouteLink, Redirect, Route, RouteProps, Switch, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { Changeset, Link } from "@scm-manager/ui-types";
import {
  CustomQueryFlexWrappedColumns,
  devices,
  ErrorPage,
  FileControlFactory,
  HealthCheckFailureDetail,
  JumpToFileButton,
  Loading,
  NavLink,
  Page,
  PrimaryContentColumn,
  RepositoryFlags,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation,
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
import CompareRoot from "../compare/CompareRoot";
import TagRoot from "../tags/container/TagRoot";
import { useIndexLinks, useRepository } from "@scm-manager/ui-api";
import styled from "styled-components";

const TagGroup = styled.span`
  & > * {
    margin-right: 0.25rem;
  }
`;

const MobileWrapped = styled.div`
  @media screen and (max-width: ${devices.mobile.width}px) {
    margin-left: auto;
  }
`;

type UrlParams = {
  namespace: string;
  name: string;
};

const useRepositoryFromUrl = (match: Match<UrlParams>) => {
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
  const [showHealthCheck, setShowHealthCheck] = useState(false);

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

  const redirectUrlFactory = binder.getExtension<extensionPoints.RepositoryRedirect>("repository.redirect", props);
  let redirectedUrl;
  if (redirectUrlFactory) {
    redirectedUrl = url + redirectUrlFactory(props);
  } else {
    redirectedUrl = url + "/code/sources/";
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

  const titleComponent = (
    <>
      <RouteLink to={`/repos/${repository.namespace}/`} className="has-text-inherit">
        {repository.namespace}
      </RouteLink>
      /{repository.name}
    </>
  );

  const extensionProps = {
    repository,
    url: urls.escapeUrlForRoute(url),
    indexLinks
  };

  const matchesBranches = (route: RouteProps) => {
    const regex = new RegExp(`${url}/branch/.+/info`);
    if (!route.location) {
      return false;
    }
    return !!route.location.pathname.match(regex);
  };

  const matchesTags = (route: RouteProps) => {
    const regex = new RegExp(`${url}/tag/.+/info`);
    if (!route.location) {
      return false;
    }
    return !!route.location.pathname.match(regex);
  };

  const matchesCode = (route: RouteProps) => {
    const regex = new RegExp(`${url}(/code)/.*`);
    if (!route.location) {
      return false;
    }
    return !!route.location.pathname.match(regex);
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

  const modal = (
    <HealthCheckFailureDetail
      closeFunction={() => setShowHealthCheck(false)}
      active={showHealthCheck}
      failures={repository.healthCheckFailures}
    />
  );

  const escapedUrl = urls.escapeUrlForRoute(url);

  return (
    <StateMenuContextProvider>
      <Page
        title={titleComponent}
        documentTitle={`${repository.namespace}/${repository.name}`}
        afterTitle={
          <MobileWrapped className="is-flex is-align-items-center">
            <ExtensionPoint name="repository.afterTitle" props={{ repository }} />
            <TagGroup className="has-text-weight-bold">
              <RepositoryFlags repository={repository} tooltipLocation="bottom" />
            </TagGroup>
          </MobileWrapped>
        }
      >
        {modal}
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Switch>
              <Redirect exact from={urls.escapeUrlForRoute(match.url)} to={urls.escapeUrlForRoute(redirectedUrl)} />

              {/* redirect pre 2.0.0-rc2 links */}
              <Redirect from={`${escapedUrl}/changeset/:id`} to={`${url}/code/changeset/:id`} />
              <Redirect exact from={`${escapedUrl}/sources`} to={`${url}/code/sources`} />
              <Redirect from={`${escapedUrl}/sources/:revision/:path*`} to={`${url}/code/sources/:revision/:path*`} />
              <Redirect exact from={`${escapedUrl}/changesets`} to={`${url}/code/changesets`} />
              <Redirect
                from={`${escapedUrl}/branch/:branch/changesets`}
                to={`${url}/code/branch/:branch/changesets/`}
              />

              <Route path={`${escapedUrl}/info`} exact>
                <RepositoryDetails repository={repository} />
              </Route>
              <Route path={`${escapedUrl}/settings/general`}>
                <EditRepo repository={repository} />
              </Route>
              <Route path={`${escapedUrl}/settings/permissions`}>
                <Permissions namespaceOrRepository={repository} />
              </Route>
              <Route exact path={`${escapedUrl}/code/changeset/:id`}>
                <ChangesetView repository={repository} fileControlFactoryFactory={fileControlFactoryFactory} />
              </Route>
              <Route path={`${escapedUrl}/code/sourceext/:extension`} exact={true}>
                <SourceExtensions repository={repository} />
              </Route>
              <Route path={`${escapedUrl}/code/sourceext/:extension/:revision/:path*`}>
                <SourceExtensions repository={repository} baseUrl={`${url}/code/sources`} />
              </Route>
              <Route path={`${escapedUrl}/code`}>
                <CodeOverview baseUrl={`${url}/code`} repository={repository} />
              </Route>
              <Route path={`${escapedUrl}/branch/:branch`}>
                <BranchRoot repository={repository} />
              </Route>
              <Route path={`${escapedUrl}/branches`} exact={true}>
                <BranchesOverview repository={repository} baseUrl={`${url}/branch`} />
              </Route>
              <Route path={`${escapedUrl}/branches/create`}>
                <CreateBranch repository={repository} />
              </Route>
              <Route path={`${escapedUrl}/tag/:tag`}>
                <TagRoot repository={repository} baseUrl={`${url}/tag`} />
              </Route>
              <Route path={`${escapedUrl}/tags`} exact={true}>
                <TagsOverview repository={repository} baseUrl={`${url}/tag`} />
              </Route>
              <Route path={`${escapedUrl}/compare/:sourceType/:sourceName`}>
                <CompareRoot repository={repository} baseUrl={`${url}/compare`} />
              </Route>
              <ExtensionPoint<extensionPoints.RepositoryRoute>
                name="repository.route"
                props={extensionProps}
                renderAll={true}
              />
            </Switch>
          </PrimaryContentColumn>
          <SecondaryNavigationColumn>
            <SecondaryNavigation label={t("repositoryRoot.menu.navigationLabel")}>
              <ExtensionPoint<extensionPoints.RepositoryNavigationTopLevel>
                name="repository.navigation.topLevel"
                props={extensionProps}
                renderAll={true}
              />
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
                label={t("repositoryRoot.menu.tagsNavL" + "ink")}
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
              <ExtensionPoint<extensionPoints.RepositoryNavigation>
                name="repository.navigation"
                props={extensionProps}
                renderAll={true}
              />
              <SubNavigation
                to={`${url}/settings/general`}
                label={t("repositoryRoot.menu.settingsNavLink")}
                title={t("repositoryRoot.menu.settingsNavLink")}
              >
                <EditRepoNavLink repository={repository} editUrl={`${url}/settings/general`} />
                <PermissionsNavLink permissionUrl={`${url}/settings/permissions`} repository={repository} />
                <ExtensionPoint<extensionPoints.RepositorySetting>
                  name="repository.setting"
                  props={extensionProps}
                  renderAll={true}
                />
              </SubNavigation>
            </SecondaryNavigation>
          </SecondaryNavigationColumn>
        </CustomQueryFlexWrappedColumns>
      </Page>
    </StateMenuContextProvider>
  );
};

export default RepositoryRoot;
