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
import { Title, Configuration } from "@scm-manager/ui-components";
import SvnConfigurationForm from "./SvnConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class SvnGlobalConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;
    return (
      <div>
        <Title title={t("scm-svn-plugin.config.title")} />
        <Configuration link={link} render={(props: any) => <SvnConfigurationForm {...props} />} />
      </div>
    );
  }
}

export default withTranslation("plugins")(SvnGlobalConfiguration);
