// @flow
import * as React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { NavLink } from "../navigation";
import { Route } from "react-router-dom";
import { translate } from "react-i18next";


class RepositoryConfigurationBinder {

  i18nNamespace: string = "plugins";

  bindGlobal(to: string, labelI18nKey: string, linkName: string, ConfigurationComponent: any) {

    // create predicate based on the link name of the index resource
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const configPredicate = (props: Object) => {
      return props.repository && props.repository._links && props.repository._links[linkName];
    };

    // create NavigationLink with translated label
    const ConfigNavLink = translate(this.i18nNamespace)(({t, url}) => {
      return <NavLink to={url + to} label={t(labelI18nKey)} />;
    });

    // bind navigation link to extension point
    binder.bind("repository.navigation", ConfigNavLink, configPredicate);


    // route for global configuration, passes the current repository to component
    const ConfigRoute = ({ url, repository }) => {
      return <Route path={url + to}
                    render={() => <ConfigurationComponent repository={repository}/>}
                    exact/>;
    };

    // bind config route to extension point
    binder.bind("repository.route", ConfigRoute, configPredicate);
  }

}

export default new RepositoryConfigurationBinder();
