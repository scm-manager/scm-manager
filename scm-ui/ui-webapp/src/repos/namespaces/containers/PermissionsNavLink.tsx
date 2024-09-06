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
import { Namespace } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  permissionUrl: string;
  namespace: Namespace;
};

class PermissionsNavLink extends React.Component<Props> {
  hasPermissionsLink = () => {
    return this.props.namespace?._links?.permissions;
  };
  render() {
    if (!this.hasPermissionsLink()) {
      return null;
    }
    const { permissionUrl, t } = this.props;
    return <NavLink to={permissionUrl} label={t("namespaceRoot.menu.permissionsNavLink")} />;
  }
}

export default withTranslation("namespaces")(PermissionsNavLink);
