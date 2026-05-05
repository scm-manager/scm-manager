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

import React, { ComponentProps, ComponentType, FC } from "react";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import { NavLink } from "../navigation";
import { type RouteObject } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Link, Links, Repository } from "@scm-manager/ui-types";
import {
  TitledGlobalSettingComponent,
  TitledNamespaceSettingComponent,
  TitledRepositorySettingComponent,
} from "./TitledSettings";
import { NAMESPACE_SUB_ROUTES, REPOSITORY_SUB_ROUTES } from "@scm-manager/ui-core";
import { useIndexLinks } from "@scm-manager/ui-api";

type GlobalRouteProps = {
  url: string;
  links: Links;
};

type RepositoryRouteProps = {
  url: string;
  repository: Repository;
};

type RepositoryNavProps = WithTranslation & { absoluteRepoUrl: string };

type NamespaceNavProps = WithTranslation & { absoluteNamespaceUrl: string };

type GlobalSettingsWrapperProps = {
  ConfigurationComponent: any;
  linkName: string;
  i18nNamespace: string;
  labelI18nKey: string;
  additionalProps?: GlobalRouteProps;
};

const GlobalSettingsWrapper: FC<GlobalSettingsWrapperProps> = ({
  ConfigurationComponent,
  linkName,
  i18nNamespace,
  labelI18nKey,
  additionalProps,
}) => {
  const links = useIndexLinks();
  const link = links[linkName];

  return (
    <TitledGlobalSettingComponent i18nNamespace={i18nNamespace} label={labelI18nKey}>
      <ConfigurationComponent link={(link as Link)?.href} {...additionalProps} />
    </TitledGlobalSettingComponent>
  );
};

class ConfigurationBinder {
  i18nNamespace = "plugins";

  navLink(
    to: string,
    labelI18nKey: string,
    t: any,
    options: Omit<ComponentProps<typeof NavLink>, "label" | "to"> = {},
  ) {
    return <NavLink to={to} label={t(labelI18nKey)} {...options} />;
  }

  route(path: string, Component: any): RouteObject {
    return { path, element: Component };
  }

  bindAdminSetting(to: string, labelI18nKey: string, linkName: string, ConfigurationComponent: any, sortKey?: string) {
    // create predicate based on the link name of the index resource
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const configPredicate = (props: any) => {
      return props.links && props.links[linkName];
    };

    // create NavigationLink with translated label
    const sanitizedTo = to.startsWith("/") ? to.substring(1) : to;
    const ConfigNavLink = withTranslation(this.i18nNamespace)(({ t }) => {
      return this.navLink(`/admin/settings/${sanitizedTo}`, labelI18nKey, t, {});
    });

    // bind navigation link to extension point
    binder.bind("admin.setting", ConfigNavLink, configPredicate, sortKey ?? linkName);

    // route for global configuration, passes the link from the index resource to component
    const ConfigRoute = ({ ...additionalProps }: GlobalRouteProps = {} as GlobalRouteProps) => {
      return this.route(
        sanitizedTo,
        <GlobalSettingsWrapper
          ConfigurationComponent={ConfigurationComponent}
          linkName={linkName}
          i18nNamespace={this.i18nNamespace}
          labelI18nKey={labelI18nKey}
          {...additionalProps}
        />,
      );
    };

    // bind config route to extension point
    binder.bind("admin.settings.route", ConfigRoute);
  }

  bindAdmin(to: string, labelI18nKey: string, icon: string, Component: React.ComponentType) {
    const AdminNavLink = withTranslation(this.i18nNamespace)(
      ({ t }: WithTranslation & extensionPoints.AdminNavigation["props"]) =>
        this.navLink("/admin/" + to, labelI18nKey, t, { icon }),
    );
    binder.bind("admin.navigation", AdminNavLink);

    const AdminRoute: extensionPoints.AdminRoute["type"] = () => this.route(to, <Component />);

    binder.bind<extensionPoints.AdminRoute>("admin.route", AdminRoute);
  }

  bindRepository(to: string, labelI18nKey: string, linkName: string, RepositoryComponent: any) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: any) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    // create NavigationLink with translated label
    const RepoNavLink = withTranslation(this.i18nNamespace)(({ t, absoluteRepoUrl }: RepositoryNavProps) => {
      return this.navLink(absoluteRepoUrl + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind("repository.navigation", RepoNavLink, repoPredicate);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ repository, ...additionalProps }: RepositoryRouteProps = {} as RepositoryRouteProps) => {
      const link = repository._links[linkName];
      if (link) {
        return this.route(
          to,
          <RepositoryComponent repository={repository} link={(link as Link).href} {...additionalProps} />,
        );
      }
    };
    // // bind config route to extension point
    binder.bind("repository.route", RepoRoute, repoPredicate);
  }

  bindRepositorySetting(
    to: string,
    labelI18nKey: string,
    linkName: string,
    RepositoryComponent: ComponentType<object>,
    sortKey?: string,
  ) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: extensionPoints.RepositorySetting["props"]) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    const sanitizedTo = to.startsWith("/") ? to.substring(1) : to;

    // create NavigationLink with translated label
    const RepoNavLink = withTranslation(this.i18nNamespace)(({ t, absoluteRepoUrl }: RepositoryNavProps) => {
      return this.navLink(`${absoluteRepoUrl}/${REPOSITORY_SUB_ROUTES.SETTINGS}/${sanitizedTo}`, labelI18nKey, t, {});
    });

    // bind navigation link to extension point
    binder.bind("repository.setting", RepoNavLink, repoPredicate, sortKey ?? linkName);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ ...additionalProps }: RepositoryRouteProps = {} as RepositoryRouteProps) => {
      return this.route(
        `settings/${sanitizedTo}`,
        <TitledRepositorySettingComponent i18nNamespace={this.i18nNamespace} label={labelI18nKey}>
          <RepositoryComponent {...additionalProps} />
        </TitledRepositorySettingComponent>,
      );
    };

    // bind config route to extension point
    binder.bind("repository.route", RepoRoute);
  }

  bindNamespaceSetting(
    to: string,
    labelI18nKey: string,
    linkName: string,
    ConfigurationComponent: ComponentType<object>,
  ) {
    const namespacePredicate = (props: extensionPoints.NamespaceSetting["props"]) =>
      props.namespace && props.namespace._links && props.namespace._links[linkName];

    const sanitizedTo = to.startsWith("/") ? to.substring(1) : to;

    const NamespaceNavLink = withTranslation(this.i18nNamespace)(({ t, absoluteNamespaceUrl }: NamespaceNavProps) => {
      return this.navLink(
        `${absoluteNamespaceUrl}/${NAMESPACE_SUB_ROUTES.SETTINGS}/${sanitizedTo}`,
        labelI18nKey,
        t,
        {},
      );
    });

    binder.bind("namespace.setting", NamespaceNavLink, namespacePredicate);

    const NamespaceRoute = ({ ...additionalProps }: NamespaceNavProps = {} as NamespaceNavProps) => {
      return this.route(
        NAMESPACE_SUB_ROUTES.SETTINGS + to,
        <TitledNamespaceSettingComponent i18nNamespace={this.i18nNamespace} label={labelI18nKey}>
          <ConfigurationComponent {...additionalProps} />
        </TitledNamespaceSettingComponent>,
      );
    };

    binder.bind("namespace.route", NamespaceRoute);
  }
}

export default new ConfigurationBinder();
