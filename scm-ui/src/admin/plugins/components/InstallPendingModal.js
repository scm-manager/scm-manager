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
import type { PluginCollection } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  onClose: () => void,
  collection: PluginCollection,

  // context props
  t: string => string
};

type State = {
  loading: boolean,
  success: boolean,
  error?: Error
};

class InstallPendingModal extends React.Component<Props, State> {
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
      return (
        <Notification type="success">
          {t("plugins.modal.successNotification")}{" "}
          <a onClick={e => window.location.reload()}>
            {t("plugins.modal.reload")}
          </a>
        </Notification>
      );
    } else {
      return (
        <Notification type="warning">
          {t("plugins.modal.restartNotification")}
        </Notification>
      );
    }
  };

  installAndRestart = () => {
    const { collection } = this.props;
    this.setState({
      loading: true
    });

    apiClient
      .post(collection._links.installPending.href)
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
    const { collection, t } = this.props;
    return (
      <>
        <div className="media">
          <div className="content">
            <p>{t("plugins.modal.installPending")}</p>
            <ul>
              {collection._embedded.plugins
                .filter(plugin => plugin.pending)
                .map(plugin => (
                  <li key={plugin.name} className="has-text-weight-bold">{plugin.name}</li>
                ))}
            </ul>
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
          label={t("plugins.modal.installAndRestart")}
          loading={loading}
          action={this.installAndRestart}
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
        title={t("plugins.modal.installAndRestart")}
        closeFunction={onClose}
        body={this.renderBody()}
        footer={this.renderFooter()}
        active={true}
      />
    );
  }
}

export default translate("admin")(InstallPendingModal);
