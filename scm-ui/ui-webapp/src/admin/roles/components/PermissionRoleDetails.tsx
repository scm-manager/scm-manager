import React from "react";
import { translate } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import PermissionRoleDetailsTable from "./PermissionRoleDetailsTable";
import { Button } from "@scm-manager/ui-components";

type Props = {
  role: RepositoryRole;
  url: string;

  // context props
  t: (p: string) => string;
};

class PermissionRoleDetails extends React.Component<Props> {
  renderEditButton() {
    const { t, url } = this.props;
    if (!!this.props.role._links.update) {
      return (
        <Button
          label={t("repositoryRole.editButton")}
          link={`${url}/edit`}
          color="primary"
        />
      );
    }
    return null;
  }

  render() {
    const { role } = this.props;

    return (
      <>
        <PermissionRoleDetailsTable role={role} />
        <hr />
        {this.renderEditButton()}
        <ExtensionPoint
          name="repositoryRole.role-details.information"
          renderAll={true}
          props={{
            role
          }}
        />
      </>
    );
  }
}

export default translate("admin")(PermissionRoleDetails);
