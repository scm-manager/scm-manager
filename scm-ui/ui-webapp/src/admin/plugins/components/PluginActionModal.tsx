import * as React from "react";
import { Button, ButtonGroup, ErrorNotification, Modal } from "@scm-manager/ui-components";
import { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import SuccessNotification from "./SuccessNotification";

type Props = {
  onClose: () => void;
  actionType: string;
  pendingPlugins?: PendingPlugins;
  installedPlugins?: PluginCollection;
  refresh: () => void;
  execute: () => Promise<any>;
  description: string;
  label: string;

  children?: React.Node;

  // context props
  t: (p: string) => string;
};

type State = {
  loading: boolean;
  success: boolean;
  error?: Error;
};

class PluginActionModal extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: false,
      success: false
    };
  }

  renderNotifications = () => {
    const { children } = this.props;
    const { error, success } = this.state;
    if (error) {
      return <ErrorNotification error={error} />;
    } else if (success) {
      return <SuccessNotification />;
    } else {
      return children;
    }
  };

  executeAction = () => {
    this.setState({
      loading: true
    });

    this.props
      .execute()
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
        {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.new.length > 0 && (
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
        {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.update.length > 0 && (
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
        {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.uninstall.length > 0 && (
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
    const { onClose, t } = this.props;
    const { loading, error, success } = this.state;
    return (
      <ButtonGroup>
        <Button
          color="warning"
          label={this.props.label}
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
        title={this.props.label}
        closeFunction={onClose}
        body={this.renderBody()}
        footer={this.renderFooter()}
        active={true}
      />
    );
  }
}

export default translate("admin")(PluginActionModal);
