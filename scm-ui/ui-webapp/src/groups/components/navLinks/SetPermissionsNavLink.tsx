import React from "react";
import { translate } from "react-i18next";
import { Group } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = {
  t: (p: string) => string;
  group: Group;
  permissionsUrl: string;
};

class ChangePermissionNavLink extends React.Component<Props> {
  render() {
    const { t, permissionsUrl } = this.props;

    if (!this.hasPermissionToSetPermission()) {
      return null;
    }
    return (
      <NavLink
        to={permissionsUrl}
        label={t("singleGroup.menu.setPermissionsNavLink")}
      />
    );
  }

  hasPermissionToSetPermission = () => {
    return this.props.group._links.permissions;
  };
}

export default translate("groups")(ChangePermissionNavLink);
