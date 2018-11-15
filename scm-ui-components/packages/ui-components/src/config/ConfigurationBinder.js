// @flow
import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { NavLink } from "../navigation";
import { Route } from "react-router-dom";
import { translate } from "react-i18next";

class ConfigurationBinder {

  i18nNamespace: string = "plugins";

  bindGlobal(to: string, labelI18nKey: string, linkName: string, ConfigurationComponent: any) {

    // create predicate based on the link name of the index resource
    // if the linkname is not available, the navigation link and the route are not bound to the extension points
    const configPredicate = (props: Object) => {
      return props.links && props.links[linkName];
    };

    // create NavigationLink with translated label
    const ConfigNavLink = translate(this.i18nNamespace)(({t}) => {
      return <NavLink to={"/config" + to} label={t(labelI18nKey)} />;
    });

    // bind navigation link to extension point
    binder.bind("config.navigation", ConfigNavLink, configPredicate);


    // route for global configuration, passes the link from the index resource to component
    const ConfigRoute = ({ url, links }) => {
      const link = links[linkName].href;
      return <Route path={url + to}
                    render={() => <ConfigurationComponent link={link}/>}
                    exact/>;
    };

    // bind config route to extension point
    binder.bind("config.route", ConfigRoute, configPredicate);
  }

}

export default new ConfigurationBinder();
