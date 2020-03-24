/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PendingPlugins } from "@scm-manager/ui-types";
import { apiClient, Notification } from "@scm-manager/ui-components";
import waitForRestart from "./waitForRestart";
import PluginActionModal from "./PluginActionModal";

type Props = WithTranslation & {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

class ExecutePendingActionModal extends React.Component<Props> {
  render() {
    const { onClose, pendingPlugins, t } = this.props;

    return (
      <PluginActionModal
        description={t("plugins.modal.executePending")}
        label={t("plugins.modal.executeAndRestart")}
        onClose={onClose}
        pendingPlugins={pendingPlugins}
        execute={this.executeAndRestart}
      >
        <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
      </PluginActionModal>
    );
  }

  executeAndRestart = () => {
    const { pendingPlugins } = this.props;
    return apiClient.post(pendingPlugins._links.execute.href).then(waitForRestart);
  };
}

export default withTranslation("admin")(ExecutePendingActionModal);
