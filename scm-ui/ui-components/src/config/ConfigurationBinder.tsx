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
import { Route } from "react-router-dom";
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import { Link, Links, Namespace, Repository } from "@scm-manager/ui-types";
import { urls } from "@scm-manager/ui-api";

type GlobalRouteProps = {
  url: string;
  links: Links;
};

type NamespaceRouteProps = {
  url: string;
  namespace: Namespace;
};

type RepositoryRouteProps = {
  url: string;
  repository: Repository;
};

type RepositoryNavProps = WithTranslation & { url: string };

type NamespaceNavProps = WithTranslation & { url: string };

class ConfigurationBinder {
  i18nNamespace = "plugins";

  navLink(
    to: string,
    labelI18nKey: string,
    t: any,
    options: Omit<ComponentProps<typeof NavLink>, "label" | "to"> = {}
  ) {
    return <NavLink to={to} label={t(labelI18nKey)} {...options} />;
  }

  route(path: string, Component: any) {
    return <Route path={urls.escapeUrlForRoute(path)}>{Component}</Route>;
  }

  bindGlobal(to: string, labelI18nKey: string, linkName: string, ConfigurationComponent: any, sortKey?: string) {
    // create predicate based on the link name of the index resource
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const configPredicate = (props: any) => {
      return props.links && props.links[linkName];
    };

    // create NavigationLink with translated label
    const ConfigNavLink = withTranslation(this.i18nNamespace)(({ t }) => {
      return this.navLink("/admin/settings" + to, labelI18nKey, t, { activeOnlyWhenExact: false });
    });

    // bind navigation link to extension point
    binder.bind("admin.setting", ConfigNavLink, configPredicate, sortKey ?? linkName);

    // route for global configuration, passes the link from the index resource to component
    const ConfigRoute = ({ url, links, ...additionalProps }: GlobalRouteProps) => {
      const link = links[linkName];
      if (link) {
        return this.route(
          url + "/settings" + to,
          <ConfigurationComponent link={(link as Link).href} {...additionalProps} />
        );
      }
    };

    // bind config route to extension point
    binder.bind("admin.route", ConfigRoute, configPredicate);
  }

  bindAdmin(
    to: string,
    labelI18nKey: string,
    icon: string,
    linkName: string,
    Component: React.ComponentType<{ link: string }>
  ) {
    const predicate = ({ links }: extensionPoints.AdminRoute["props"]) => links[linkName];

    const AdminNavLink = withTranslation(this.i18nNamespace)(
      ({ t, url }: WithTranslation & extensionPoints.AdminNavigation["props"]) =>
        this.navLink(url + to, labelI18nKey, t, { icon })
    );

    const AdminRoute: extensionPoints.AdminRoute["type"] = ({ links, url }) =>
      this.route(url + to, <Component link={(links[linkName] as Link).href} />);

    binder.bind<extensionPoints.AdminRoute>("admin.route", AdminRoute, predicate);
    binder.bind<extensionPoints.AdminNavigation>("admin.navigation", AdminNavLink, predicate);
  }

  bindRepository(to: string, labelI18nKey: string, linkName: string, RepositoryComponent: any) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: any) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    // create NavigationLink with translated label
    const RepoNavLink = withTranslation(this.i18nNamespace)(({ t, url }: RepositoryNavProps) => {
      return this.navLink(url + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind("repository.navigation", RepoNavLink, repoPredicate);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ url, repository, ...additionalProps }: RepositoryRouteProps) => {
      const link = repository._links[linkName];
      if (link) {
        return this.route(
          url + to,
          <RepositoryComponent repository={repository} link={(link as Link).href} {...additionalProps} />
        );
      }
    };

    // bind config route to extension point
    binder.bind("repository.route", RepoRoute, repoPredicate);
  }

  bindRepositorySetting(to: string, labelI18nKey: string, linkName: string, RepositoryComponent: any, sortKey?: string) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: any) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    // create NavigationLink with translated label
    const RepoNavLink = withTranslation(this.i18nNamespace)(({ t, url }: RepositoryNavProps) => {
      return this.navLink(url + "/settings" + to, labelI18nKey, t, { activeOnlyWhenExact: false });
    });

    // bind navigation link to extension point
    binder.bind("repository.setting", RepoNavLink, repoPredicate, sortKey ?? linkName);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ url, repository, ...additionalProps }: RepositoryRouteProps) => {
      const link = repository._links[linkName];
      if (link) {
        return this.route(
          urls.unescapeUrlForRoute(url) + "/settings" + to,
          <RepositoryComponent repository={repository} link={(link as Link).href} {...additionalProps} />
        );
      }
    };

    // bind config route to extension point
    binder.bind("repository.route", RepoRoute, repoPredicate);
  }

  bindNamespaceSetting(
    to: string,
    labelI18nKey: string,
    linkName: string,
    ConfigurationComponent: ComponentType<{ link: string; namespace: Namespace }>
  ) {
    const namespacePredicate = (props: (extensionPoints.NamespaceSetting | extensionPoints.NamespaceRoute)["props"]) =>
      props.namespace && props.namespace._links && props.namespace._links[linkName];

    const NamespaceNavLink: FC<extensionPoints.NamespaceRoute["props"]> = ({ url }) => {
      const [t] = useTranslation(this.i18nNamespace);
      return this.navLink(url + "/settings" + to, labelI18nKey, t, { activeOnlyWhenExact: false });
    };

    binder.bind<extensionPoints.NamespaceSetting>("namespace.setting", NamespaceNavLink, namespacePredicate);

    const NamespaceRoute: FC<extensionPoints.NamespaceRoute["props"]> = ({ url, namespace, ...additionalProps }) => {
      const link = namespace._links[linkName];
      if (link) {
        return this.route(
          urls.unescapeUrlForRoute(url) + "/settings" + to,
          <ConfigurationComponent namespace={namespace} link={(link as Link).href} {...additionalProps} />
        );
      }
      return null;
    };

    binder.bind<extensionPoints.NamespaceRoute>("namespace.route", NamespaceRoute, namespacePredicate);
  }
}

export default new ConfigurationBinder();
