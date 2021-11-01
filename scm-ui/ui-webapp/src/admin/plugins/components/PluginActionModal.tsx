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
import * as React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import { Button, ButtonGroup, ErrorNotification, Modal } from "@scm-manager/ui-components";
import SuccessNotification from "./SuccessNotification";

type Props = WithTranslation & {
  onClose: () => void;
  pendingPlugins?: PendingPlugins;
  installedPlugins?: PluginCollection;
  execute: () => void;
  description: string;
  label: string;
  loading: boolean;
  error?: Error | null;
  success: boolean;
  children?: React.Node;
};

class PluginActionModal extends React.Component<Props> {
  renderNotifications = () => {
    const { children, error, success } = this.props;
    if (error) {
      return <ErrorNotification error={error} />;
    } else if (success) {
      return <SuccessNotification />;
    } else {
      return children;
    }
  };

  renderModalContent = () => {
    return (
      <>
        {this.renderUpdatable()}
        {this.renderInstallQueue()}
        {this.renderUpdateQueue()}
        {this.renderUninstallQueue()}
      </>
    );
  };

  renderUpdatable = () => {
    const { installedPlugins, t } = this.props;
    return (
      <>
        {installedPlugins && installedPlugins._embedded && installedPlugins._embedded.plugins && (
          <>
            <strong>{t("plugins.modal.updateQueue")}</strong>
            <ul>
              {installedPlugins._embedded.plugins
                .filter((plugin) => plugin._links && plugin._links.update)
                .map((plugin) => (
                  <li key={plugin.name}>{plugin.name}</li>
                ))}
            </ul>
          </>
        )}
      </>
    );
  };

  renderInstallQueue = () => {
    const { pendingPlugins, t } = this.props;
    return (
      <>
        {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.new.length > 0 && (
          <>
            <strong>{t("plugins.modal.installQueue")}</strong>
            <ul>
              {pendingPlugins._embedded.new.map((plugin) => (
                <li key={plugin.name}>{plugin.name}</li>
              ))}
            </ul>
          </>
        )}
      </>
    );
  };

  renderUpdateQueue = () => {
    const { pendingPlugins, t } = this.props;
    return (
      <>
        {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.update.length > 0 && (
          <>
            <strong>{t("plugins.modal.updateQueue")}</strong>
            <ul>
              {pendingPlugins._embedded.update.map((plugin) => (
                <li key={plugin.name}>{plugin.name}</li>
              ))}
            </ul>
          </>
        )}
      </>
    );
  };

  renderUninstallQueue = () => {
    const { pendingPlugins, t } = this.props;
    return (
      <>
        {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.uninstall.length > 0 && (
          <>
            <strong>{t("plugins.modal.uninstallQueue")}</strong>
            <ul>
              {pendingPlugins._embedded.uninstall.map((plugin) => (
                <li key={plugin.name}>{plugin.name}</li>
              ))}
            </ul>
          </>
        )}
      </>
    );
  };

  renderBody = () => {
    return (
      <>
        <div className="media">
          <div className="content">
            <p>{this.props.description}</p>
            {this.renderModalContent()}
          </div>
        </div>
        <div className="media">{this.renderNotifications()}</div>
      </>
    );
  };

  renderFooter = () => {
    const { onClose, t, loading, error, success } = this.props;
    return (
      <ButtonGroup>
        <Button
          color="warning"
          label={this.props.label}
          loading={loading}
          action={this.props.execute}
          disabled={!!error || success}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  render() {
    const { onClose } = this.props;
    return (
      <Modal
        title={this.props.label}
        closeFunction={onClose}
        body={this.renderBody()}
        footer={this.renderFooter()}
        active={true}
      />
    );
  }
}

export default withTranslation("admin")(PluginActionModal);
