import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { NavLink } from "../navigation";
import { Route } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository, Links, Link } from "@scm-manager/ui-types";

type GlobalRouteProps = {
  url: string;
  links: Links;
};

type RepositoryRouteProps = {
  url: string;
  repository: Repository;
};

type RepositoryNavProps = WithTranslation & { url: string };

class ConfigurationBinder {
  i18nNamespace = "plugins";

  navLink(to: string, labelI18nKey: string, t: any) {
    return <NavLink to={to} label={t(labelI18nKey)} />;
  }

  route(path: string, Component: any) {
    return <Route path={path} render={() => Component} exact />;
  }

  bindGlobal(to: string, labelI18nKey: string, linkName: string, ConfigurationComponent: any) {
    // create predicate based on the link name of the index resource
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const configPredicate = (props: any) => {
      return props.links && props.links[linkName];
    };

    // create NavigationLink with translated label
    const ConfigNavLink = withTranslation(this.i18nNamespace)(({ t }) => {
      return this.navLink("/admin/settings" + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind("admin.setting", ConfigNavLink, configPredicate);

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

  bindRepositorySetting(to: string, labelI18nKey: string, linkName: string, RepositoryComponent: any) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: any) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    // create NavigationLink with translated label
    const RepoNavLink = withTranslation(this.i18nNamespace)(({ t, url }: RepositoryNavProps) => {
      return this.navLink(url + "/settings" + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind("repository.setting", RepoNavLink, repoPredicate);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ url, repository, ...additionalProps }: RepositoryRouteProps) => {
      const link = repository._links[linkName];
      if (link) {
        return this.route(
          url + "/settings" + to,
          <RepositoryComponent repository={repository} link={(link as Link).href} {...additionalProps} />
        );
      }
    };

    // bind config route to extension point
    binder.bind("repository.route", RepoRoute, repoPredicate);
  }
}

export default new ConfigurationBinder();
