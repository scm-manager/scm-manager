//@flow
import React from "react";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  permissionUrl: string,
  t: string => string,
  repository: Repository
};

class PermissionsNavLink extends React.Component<Props> {
  hasPermissionsLink = () => {
    return this.props.repository._links.permissions;
  };
  render() {
    if (!this.hasPermissionsLink()) {
      return null;
    }
    const { permissionUrl, t } = this.props;
    return (
      <NavLink icon="fas fa-lock" to={permissionUrl} label={t("repository-root.permissions")} />
    );
  }
}

export default translate("repos")(PermissionsNavLink);
