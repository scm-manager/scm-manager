import React from "react";
import { apiClient, Button, ButtonGroup, ErrorNotification, Modal, Notification } from "@scm-manager/ui-components";
import { PendingPlugins } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import waitForRestart from "./waitForRestart";
import SuccessNotification from "./SuccessNotification";

type Props = {
  onClose: () => void;
  pendingPlugins: PendingPlugins;

  // context props
  t: (p: string) => string;
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

  renderInstallQueue = () => {
    const { pendingPlugins, t } = this.props;
    return (
      <>
        {pendingPlugins._embedded && pendingPlugins._embedded.new.length > 0 && (
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
        {pendingPlugins._embedded && pendingPlugins._embedded.update.length > 0 && (
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
        {pendingPlugins._embedded && pendingPlugins._embedded.uninstall.length > 0 && (
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

  renderBody = () => {
    const { t } = this.props;
    return (
      <>
        <div className="media">
          <div className="content">
            <p>{t("plugins.modal.executePending")}</p>
            {this.renderInstallQueue()}
            {this.renderUpdateQueue()}
            {this.renderUninstallQueue()}
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

export default translate("admin")(ExecutePendingModal);
