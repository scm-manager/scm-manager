import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { RepositoryRole } from "@scm-manager/ui-types";
import { Button, Level } from "@scm-manager/ui-components";
import PermissionRoleDetailsTable from "./PermissionRoleDetailsTable";

type Props = WithTranslation & {
  role: RepositoryRole;
  url: string;
};

class PermissionRoleDetails extends React.Component<Props> {
  renderEditButton() {
    const { t, url } = this.props;
    if (!!this.props.role._links.update) {
      return (
        <>
          <hr />
          <Level right={<Button label={t("repositoryRole.editButton")} link={`${url}/edit`} color="primary" />} />
        </>
      );
    }
    return null;
  }

  render() {
    const { role } = this.props;

    return (
      <>
        <PermissionRoleDetailsTable role={role} />
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

export default withTranslation("admin")(PermissionRoleDetails);
