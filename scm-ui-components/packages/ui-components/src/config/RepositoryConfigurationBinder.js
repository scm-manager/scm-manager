// @flow
import * as React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { RepositoryNavLink } from "../navigation";
import { Route } from "react-router-dom";
import { translate } from "react-i18next";


class RepositoryConfigurationBinder {

  i18nNamespace: string = "plugins";

  bindRepository(to: string, labelI18nKey: string, linkName: string, RepositoryComponent: any) {

    // create predicate based on the link name of the current repository route
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const repoPredicate = (props: Object) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    // create NavigationLink with translated label
    const RepoNavLink = translate(this.i18nNamespace)(({t, url}) => {
      return <RepositoryNavLink to={url + to} label={t(labelI18nKey)} />;
    });

    // bind navigation link to extension point
    binder.bind("repository.navigation", RepoNavLink, repoPredicate);


    // route for global configuration, passes the current repository to component
    const RepoRoute = ({ url, repository }) => {
      return <Route path={url + to}
                    render={() => <RepositoryComponent repository={repository}/>}
                    exact/>;
    };

    // bind config route to extension point
    binder.bind("repository.route", RepoRoute, repoPredicate);
  }


}

export default new RepositoryConfigurationBinder();
