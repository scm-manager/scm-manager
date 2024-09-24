/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
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
        <ExtensionPoint<extensionPoints.RepositoryRoleDetailsInformation>
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
