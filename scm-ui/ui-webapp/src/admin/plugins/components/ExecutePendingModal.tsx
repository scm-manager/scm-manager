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
import { apiClient, Button, ButtonGroup, ErrorNotification, Modal, Notification } from "@scm-manager/ui-components";
import { PendingPlugins } from "@scm-manager/ui-types";
import { WithTranslation, withTranslation } from "react-i18next";
import waitForRestart from "./waitForRestart";
import SuccessNotification from "./SuccessNotification";
import PendingPluginsQueue from "./PendingPluginsQueue";

type Props = WithTranslation & {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

type State = {
  loading: boolean;
  success: boolean;
  error?: Error;
};

class ExecutePendingModal extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: false,
      success: false
    };
  }

  renderNotifications = () => {
    const { t } = this.props;
    const { error, success } = this.state;
    if (error) {
      return <ErrorNotification error={error} />;
    } else if (success) {
      return <SuccessNotification />;
    } else {
      return <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>;
    }
  };

  executeAndRestart = () => {
    const { pendingPlugins } = this.props;
    this.setState({
      loading: true
    });

    apiClient
      .post(pendingPlugins._links.execute.href)
      .then(waitForRestart)
      .then(() => {
        this.setState({
          success: true,
          loading: false,
          error: undefined
        });
      })
      .catch(error => {
        this.setState({
          success: false,
          loading: false,
          error: error
        });
      });
  };

  renderBody = () => {
    const { pendingPlugins, t } = this.props;
    return (
      <>
        <div className="media">
          <div className="content">
            <p>{t("plugins.modal.executePending")}</p>
            <PendingPluginsQueue pendingPlugins={pendingPlugins} />
          </div>
        </div>
        <div className="media">{this.renderNotifications()}</div>
      </>
    );
  };

  renderFooter = () => {
    const { onClose, t } = this.props;
    const { loading, error, success } = this.state;
    return (
      <ButtonGroup>
        <Button
          color="warning"
          label={t("plugins.modal.executeAndRestart")}
          loading={loading}
          action={this.executeAndRestart}
          disabled={error || success}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  render() {
    const { onClose, t } = this.props;
    return (
      <Modal
        title={t("plugins.modal.executeAndRestart")}
        closeFunction={onClose}
        body={this.renderBody()}
        footer={this.renderFooter()}
        active={true}
      />
    );
  }
}

export default withTranslation("admin")(ExecutePendingModal);
