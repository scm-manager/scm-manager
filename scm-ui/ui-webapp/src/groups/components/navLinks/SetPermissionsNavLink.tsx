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
