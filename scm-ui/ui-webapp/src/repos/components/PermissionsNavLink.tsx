import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  permissionUrl: string;
  repository: Repository;
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
    return <NavLink to={permissionUrl} label={t("repositoryRoot.menu.permissionsNavLink")} />;
  }
}

export default withTranslation("repos")(PermissionsNavLink);
