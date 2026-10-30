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

import React, { FC, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouteLink, Outlet, useNavigate } from "react-router-dom";
import styled from "styled-components";
import { useIndexLinks } from "@scm-manager/ui-api";
import {
  CustomQueryFlexWrappedColumns,
  devices,
  HealthCheckFailureDetail,
  NavLink,
  Page,
  PrimaryContentColumn,
  RepositoryFlags,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  SubNavigation,
} from "@scm-manager/ui-components";
import {
  getRepositoryBaseUrl,
  REPOSITORY_CODE_SUB_ROUTES,
  REPOSITORY_COLLECTION_BASE_ROUTE,
  REPOSITORY_SETTINGS_SUB_ROUTES,
  REPOSITORY_SUB_ROUTES,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  useShortcut,
} from "@scm-manager/ui-core";
import { ExtensionPoint, extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import EditRepoNavLink from "../components/EditRepoNavLink";
import PermissionsNavLink from "../components/PermissionsNavLink";
import RepositoryNavLink from "../components/RepositoryNavLink";

const MobileWrapped = styled.div`
  @media screen and (max-width: ${devices.mobile.width}px) {
    margin-left: auto;
  }
`;

const RepositoryRoot: FC = () => {
  const repository = useRepositoryFromParams();
  const [t] = useTranslation("repos");
  const navigate = useNavigate();
  const [showHealthCheck, setShowHealthCheck] = useState(false);
  const indexLinks = useIndexLinks();
  const absoluteRepoUrl = useCurrentRepositoryBaseUrl();
  const binder = useBinder();

  const extensionProps = {
    repository,
    indexLinks,
    absoluteRepoUrl,
  };

  const hasRepositorySettings =
    !!repository._links.update ||
    !!repository._links.permissions ||
    binder.hasExtension<extensionPoints.RepositorySetting>("repository.setting", extensionProps);

  const codeLinkname = useMemo(() => {
    if (repository?._links?.sources) {
      return "sources";
    }
    if (repository?._links?.changesets) {
      return "changesets";
    }
    return "";
  }, [repository]);

  useShortcut(
    "g i",
    () => {
      navigate(`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.INFO}`);
    },
    {
      description: t("shortcuts.info"),
    },
  );
  useShortcut(
    "g b",
    () => {
      navigate(`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.BRANCHES}/`);
    },
    {
      active: !!repository?._links["branches"],
      description: t("shortcuts.branches"),
    },
  );
  useShortcut(
    "g t",
    () => {
      navigate(`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.TAGS}/`);
    },
    {
      active: !!repository?._links["tags"],
      description: t("shortcuts.tags"),
    },
  );
  useShortcut(
    "g c",
    () => {
      navigate(evaluateDestinationForCodeLink());
    },
    {
      active: !!repository?._links[codeLinkname],
      description: t("shortcuts.code"),
    },
  );
  useShortcut(
    "g s",
    () => {
      navigate(`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.SETTINGS}/${REPOSITORY_SETTINGS_SUB_ROUTES.GENERAL}`);
    },
    {
      active: hasRepositorySettings,
      description: t("shortcuts.settings"),
    },
  );

  const titleComponent = (
    <>
      <RouteLink to={`/${REPOSITORY_COLLECTION_BASE_ROUTE}/${repository.namespace}/`} className="has-text-inherit mr-1">
        {repository.namespace}
      </RouteLink>
      /
      <RouteLink to={getRepositoryBaseUrl(repository)} className="has-text-inherit ml-1">
        {repository.name}
      </RouteLink>
    </>
  );

  const evaluateDestinationForCodeLink = () => {
    if (repository?._links?.sources) {
      return `${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/`;
    }
    return `${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}`;
  };

  const modal = (
    <HealthCheckFailureDetail
      closeFunction={() => setShowHealthCheck(false)}
      active={showHealthCheck}
      failures={repository.healthCheckFailures}
    />
  );

  return (
    <Page
      title={titleComponent}
      documentTitle={`${repository.namespace}/${repository.name}`}
      afterTitle={
        <MobileWrapped className="is-flex is-align-items-center has-gap-2">
          <ExtensionPoint name="repository.afterTitle" props={{ repository }} />
          <RepositoryFlags className="has-text-weight-bold" repository={repository} tooltipLocation="bottom" />
        </MobileWrapped>
      }
    >
      {modal}
      <ExtensionPoint<extensionPoints.RepositoryBanner>
        name="repository.banner"
        props={{ repository }}
        renderAll={true}
      />
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Outlet />
        </PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("repositoryRoot.menu.navigationLabel")}>
            <RepositoryNavLink
              repository={repository}
              linkName={codeLinkname}
              to={evaluateDestinationForCodeLink()}
              icon="fas fa-code"
              label={t("repositoryRoot.menu.sourcesNavLink")}
              title={t("repositoryRoot.menu.sourcesNavLink")}
              activeWhenMatch={/(\/code)\/.*|^compare\//}
            />
            <ExtensionPoint<extensionPoints.RepositoryNavigationTopLevel>
              name="repository.navigation.topLevel"
              props={extensionProps}
              renderAll={true}
            />
            <NavLink
              to={`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.INFO}`}
              icon="fas fa-info-circle"
              label={t("repositoryRoot.menu.informationNavLink")}
              title={t("repositoryRoot.menu.informationNavLink")}
            />
            <RepositoryNavLink
              repository={repository}
              linkName="branches"
              to={`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.BRANCHES}/`}
              icon="fas fa-code-branch"
              label={t("repositoryRoot.menu.branchesNavLink")}
              title={t("repositoryRoot.menu.branchesNavLink")}
              activeWhenMatch={/branch\/.+\/info/}
            />
            <RepositoryNavLink
              repository={repository}
              linkName="tags"
              to={`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.TAGS}/`}
              icon="fas fa-tags"
              label={t("repositoryRoot.menu.tagsNavLink")}
              title={t("repositoryRoot.menu.tagsNavLink")}
              activeWhenMatch={/tag\/.+\/info/}
            />
            <ExtensionPoint<extensionPoints.RepositoryNavigation>
              name="repository.navigation"
              props={extensionProps}
              renderAll={true}
            />
            {hasRepositorySettings && (
              <SubNavigation
                to={`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.SETTINGS}/${REPOSITORY_SETTINGS_SUB_ROUTES.GENERAL}`}
                label={t("repositoryRoot.menu.settingsNavLink")}
                title={t("repositoryRoot.menu.settingsNavLink")}
              >
                <EditRepoNavLink
                  repository={repository}
                  editUrl={`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.SETTINGS}/${REPOSITORY_SETTINGS_SUB_ROUTES.GENERAL}`}
                />
                <PermissionsNavLink
                  permissionUrl={`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.SETTINGS}/${REPOSITORY_SETTINGS_SUB_ROUTES.PERMISSIONS}`}
                  repository={repository}
                />
                <ExtensionPoint<extensionPoints.RepositorySetting>
                  name="repository.setting"
                  props={extensionProps}
                  renderAll
                />
              </SubNavigation>
            )}
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default RepositoryRoot;
