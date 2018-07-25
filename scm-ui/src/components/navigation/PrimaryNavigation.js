//@flow
import React from "react";
import { translate } from "react-i18next";
import PrimaryNavigationLink from "./PrimaryNavigationLink";

type Props = {
  t: string => string
};

class PrimaryNavigation extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <nav className="tabs is-boxed">
        <ul>
          <PrimaryNavigationLink
            to="/"
            activeOnlyWhenExact={true}
            label={t("primary-navigation.repositories")}
          />
          <PrimaryNavigationLink
            to="/users"
            label={t("primary-navigation.users")}
          />
          <PrimaryNavigationLink
            to="/logout"
            label={t("primary-navigation.logout")}
          />
        </ul>
      </nav>
    );
  }
}

export default translate("commons")(PrimaryNavigation);
