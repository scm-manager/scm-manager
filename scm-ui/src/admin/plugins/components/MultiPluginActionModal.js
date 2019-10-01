// @flow
import React from "react";
import {
  apiClient,
  Button,
  ButtonGroup,
  ErrorNotification,
  Modal,
  Notification
} from "@scm-manager/ui-components";
import type { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import waitForRestart from "./waitForRestart";
import SuccessNotification from "./SuccessNotification";
import { MultiPluginActionType } from "./MultiPluginAction";

type Props = {
  onClose: () => void,
  actionType: string,
  pendingPlugins?: PendingPlugins,
  installedPlugins?: PluginCollection,
  refresh: () => void,

  // context props
  t: string => string
};

type State = {
  loading: boolean,
  success: boolean,
  error?: Error
};

class MultiPluginActionModal extends React.Component<Props, State> {
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
      return (
        <Notification type="warning">
          {t("plugins.modal.restartNotification")}
        </Notification>
      );
    }
  };

  executeAction = () => {
    const { actionType } = this.props;

    if (actionType === MultiPluginActionType.EXECUTE_PENDING) {
      this.executeAndRestart();
    } else if (actionType === MultiPluginActionType.CANCEL_PENDING) {
      this.cancelPending();
    } else if (actionType === MultiPluginActionType.UPDATE_ALL) {
      this.updateAll();
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
          loading: false
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

  cancelPending = () => {
    const { pendingPlugins } = this.props;
    this.setState({
      loading: true
    });

    apiClient
      .post(pendingPlugins._links.cancel.href)
      .then(() => this.refresh())
      .catch(error => {
        this.setState({
          success: false,
          loading: false,
          error: error
        });
      });
  };

  updateAll = () => {
    const { installedPlugins } = this.props;
    this.setState({
      loading: true
    });

    apiClient
      .post(installedPlugins._links.update.href)
      .then(() => this.refresh());
  };

  refresh = () => {
    this.props.refresh();
    this.props.onClose();
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
        {installedPlugins &&
          installedPlugins._embedded &&
          installedPlugins._embedded.plugins && (
            <>
              <strong>{t("plugins.modal.updateQueue")}</strong>
              <ul>
                {installedPlugins._embedded.plugins
                  .filter(plugin => plugin._links && plugin._links.update)
                  .map(plugin => (
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
        {pendingPlugins &&
          pendingPlugins._embedded &&
          pendingPlugins._embedded.new.length > 0 && (
            <>
              <strong>{t("plugins.modal.installQueue")}</strong>
              <ul>
                {pendingPlugins._embedded.new.map(plugin => (
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
        {pendingPlugins &&
          pendingPlugins._embedded &&
          pendingPlugins._embedded.update.length > 0 && (
            <>
              <strong>{t("plugins.modal.updateQueue")}</strong>
              <ul>
                {pendingPlugins._embedded.update.map(plugin => (
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
        {pendingPlugins &&
          pendingPlugins._embedded &&
          pendingPlugins._embedded.uninstall.length > 0 && (
            <>
              <strong>{t("plugins.modal.uninstallQueue")}</strong>
              <ul>
                {pendingPlugins._embedded.uninstall.map(plugin => (
                  <li key={plugin.name}>{plugin.name}</li>
                ))}
              </ul>
            </>
          )}
      </>
    );
  };

  renderDescription = () => {
    const { t, actionType } = this.props;

    if (actionType === MultiPluginActionType.EXECUTE_PENDING) {
      return t("plugins.modal.executePending");
    } else if (actionType === MultiPluginActionType.CANCEL_PENDING) {
      return t("plugins.modal.cancelPending");
    } else if (actionType === MultiPluginActionType.UPDATE_ALL) {
      return t("plugins.modal.updateAllInfo");
    }
  };

  renderLabel = () => {
    const { t, actionType } = this.props;

    if (actionType === MultiPluginActionType.EXECUTE_PENDING) {
      return t("plugins.modal.executeAndRestart");
    } else if (actionType === MultiPluginActionType.CANCEL_PENDING) {
      return t("plugins.cancelPending");
    } else if (actionType === MultiPluginActionType.UPDATE_ALL) {
      return t("plugins.updateAll");
    }
  };

  renderBody = () => {
    const { actionType } = this.props;
    const { error } = this.state;
    return (
      <>
        <div className="media">
          <div className="content">
            <p>{this.renderDescription()}</p>
            {this.renderModalContent()}
          </div>
        </div>
        {!!error && (
          <div className="media">
            <ErrorNotification error={error} />
          </div>
        )}
        {actionType === MultiPluginActionType.EXECUTE_PENDING && (
          <div className="media">{this.renderNotifications()}</div>
        )}
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
          label={this.renderLabel()}
          loading={loading}
          action={this.executeAction}
          disabled={error || success}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  render() {
    const { onClose } = this.props;
    return (
      <Modal
        title={this.renderLabel()}
        closeFunction={onClose}
        body={this.renderBody()}
        footer={this.renderFooter()}
        active={true}
      />
    );
  }
}

export default translate("admin")(MultiPluginActionModal);
