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
import { Notification } from "@scm-manager/ui-components";
import { PluginAction } from "../containers/PluginsOverview";

type Props = WithTranslation & {
  pluginAction?: string;
};

class InstallSuccessNotification extends React.Component<Props> {
  createMessageForPluginAction = () => {
    const { pluginAction, t } = this.props;
    if (pluginAction === PluginAction.INSTALL) {
      return t("plugins.modal.installedNotification");
    } else if (pluginAction === PluginAction.UPDATE) {
      return t("plugins.modal.updatedNotification");
    } else if (pluginAction === PluginAction.UNINSTALL) {
      return t("plugins.modal.uninstalledNotification");
    }
    return t("plugins.modal.executedChangesNotification");
  };

  render() {
    return <Notification type="success">{this.createMessageForPluginAction()}</Notification>;
  }
}

export default withTranslation("admin")(InstallSuccessNotification);
