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
import { RepositoryRole } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  role: RepositoryRole;
};

class AvailableVerbs extends React.Component<Props> {
  render() {
    const { role, t } = this.props;

    let verbs = null;
    if (role.verbs.length > 0) {
      verbs = (
        <td className="p-0">
          <ul>
            {role.verbs.map((verb, key) => {
              return <li key={key}>{t("verbs.repository." + verb + ".displayName")}</li>;
            })}
          </ul>
        </td>
      );
    }
    return verbs;
  }
}

export default withTranslation("plugins")(AvailableVerbs);
