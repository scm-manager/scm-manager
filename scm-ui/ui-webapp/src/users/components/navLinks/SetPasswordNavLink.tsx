import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  user: User;
  passwordUrl: string;
};

class ChangePasswordNavLink extends React.Component<Props> {
  render() {
    const { t, passwordUrl } = this.props;

    if (!this.hasPermissionToSetPassword()) {
      return null;
    }
    return <NavLink to={passwordUrl} label={t("singleUser.menu.setPasswordNavLink")} />;
  }

  hasPermissionToSetPassword = () => {
    return this.props.user._links.password;
  };
}

export default withTranslation("users")(ChangePasswordNavLink);
