//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Role } from "@scm-manager/ui-types";
import SystemRoleTag from "./SystemRoleTag";

type Props = {
  role: Role,

  // context props
  t: string => string,
};

class PermissionRoleDetail extends React.Component<Props> {
  render() {
    const { role, t } = this.props;

    return (
      <div className="media">
        <div className="media-content subtitle">
          <strong>{t("role.name")}:</strong> {role.name}{" "}
          <SystemRoleTag system={!role._links.update} />
        </div>
      </div>
    );
  }
}

export default translate("config")(PermissionRoleDetail);
