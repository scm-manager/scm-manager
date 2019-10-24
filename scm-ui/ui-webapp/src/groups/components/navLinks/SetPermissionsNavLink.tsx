import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Group } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  group: Group;
  permissionsUrl: string;
};

class ChangePermissionNavLink extends React.Component<Props> {
  render() {
    const { t, permissionsUrl } = this.props;

    if (!this.hasPermissionToSetPermission()) {
      return null;
    }
    return <NavLink to={permissionsUrl} label={t("singleGroup.menu.setPermissionsNavLink")} />;
  }

  hasPermissionToSetPermission = () => {
    return this.props.group._links.permissions;
  };
}

export default withTranslation("groups")(ChangePermissionNavLink);
