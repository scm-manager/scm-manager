//@flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  user: User,
  passwordUrl: String
};

class ChangePasswordNavLink extends React.Component<Props> {
  render() {
    const { t, passwordUrl } = this.props;

    if (!this.hasPermissionToSetPassword()) {
      return null;
    }
    return <NavLink label={t("set-password-button.label")} to={passwordUrl} />;
  }

  hasPermissionToSetPassword = () => {
    return this.props.user._links.password;
  };
}

export default translate("users")(ChangePasswordNavLink);
