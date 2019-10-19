import React from 'react';
import { binder } from '@scm-manager/ui-extensions';
import { NavLink } from '../navigation';
import { Route } from 'react-router-dom';
import { translate } from 'react-i18next';

class ConfigurationBinder {
  i18nNamespace: string = 'plugins';

  navLink(to: string, labelI18nKey: string, t: any) {
    return <NavLink to={to} label={t(labelI18nKey)} />;
  }

  route(path: string, Component: any) {
    return <Route path={path} render={() => Component} exact />;
  }

  bindGlobal(
    to: string,
    labelI18nKey: string,
    linkName: string,
    ConfigurationComponent: any,
  ) {
    // create predicate based on the link name of the index resource
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const configPredicate = (props: object) => {
      return props.links && props.links[linkName];
    };

    // create NavigationLink with translated label
    const ConfigNavLink = translate(this.i18nNamespace)(({ t }) => {
      return this.navLink('/admin/settings' + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind('admin.setting', ConfigNavLink, configPredicate);

    // route for global configuration, passes the link from the index resource to component
    const ConfigRoute = ({ url, links, ...additionalProps }) => {
      const link = links[linkName].href;
      return this.route(
        url + '/settings' + to,
        <ConfigurationComponent link={link} {...additionalProps} />,
      );
    };

    // bind config route to extension point
    binder.bind('admin.route', ConfigRoute, configPredicate);
  }

  bindRepository(
    to: string,
    labelI18nKey: string,
    linkName: string,
    RepositoryComponent: any,
  ) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: object) => {
      return (
        props.repository &&
        props.repository._links &&
        props.repository._links[linkName]
      );
    };

    // create NavigationLink with translated label
    const RepoNavLink = translate(this.i18nNamespace)(({ t, url }) => {
      return this.navLink(url + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind('repository.navigation', RepoNavLink, repoPredicate);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ url, repository, ...additionalProps }) => {
      const link = repository._links[linkName].href;
      return this.route(
        url + to,
        <RepositoryComponent
          repository={repository}
          link={link}
          {...additionalProps}
        />,
      );
    };

    // bind config route to extension point
    binder.bind('repository.route', RepoRoute, repoPredicate);
  }

  bindRepositorySetting(
    to: string,
    labelI18nKey: string,
    linkName: string,
    RepositoryComponent: any,
  ) {
    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: object) => {
      return (
        props.repository &&
        props.repository._links &&
        props.repository._links[linkName]
      );
    };

    // create NavigationLink with translated label
    const RepoNavLink = translate(this.i18nNamespace)(({ t, url }) => {
      return this.navLink(url + '/settings' + to, labelI18nKey, t);
    });

    // bind navigation link to extension point
    binder.bind('repository.setting', RepoNavLink, repoPredicate);

    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ url, repository, ...additionalProps }) => {
      const link = repository._links[linkName].href;
      return this.route(
        url + '/settings' + to,
        <RepositoryComponent
          repository={repository}
          link={link}
          {...additionalProps}
        />,
      );
    };

    // bind config route to extension point
    binder.bind('repository.route', RepoRoute, repoPredicate);
  }
}

export default new ConfigurationBinder();
