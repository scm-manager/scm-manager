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
import ArrayConfigTable from "./ArrayConfigTable";
import { ConfigChangeHandler } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  proxyExcludes: string[];
  onChange: ConfigChangeHandler;
  disabled: boolean;
};

class ProxyExcludesTable extends React.Component<Props> {
  render() {
    const { proxyExcludes, disabled, t } = this.props;
    return (
      <ArrayConfigTable
        items={proxyExcludes}
        label={t("proxySettings.excludesTable.label")}
        removeLabel={t("proxySettings.excludesTable.deleteButton")}
        onRemove={this.removeEntry}
        disabled={disabled}
        helpText={t("proxySettings.excludesTable.helpText")}
      />
    );
  }

  removeEntry = (newExcludes: string[]) => {
    this.props.onChange(true, newExcludes, "proxyExcludes");
  };
}

export default withTranslation("config")(ProxyExcludesTable);
