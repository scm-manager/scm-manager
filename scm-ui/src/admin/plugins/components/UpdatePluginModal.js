//@flow
import React from "react";
import { compose } from "redux";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import {
  apiClient,
  Button,
  ButtonGroup,
  Checkbox,
  ErrorNotification,
  Modal,
  Notification
} from "@scm-manager/ui-components";
import classNames from "classnames";
import waitForRestart from "./waitForRestart";
import SuccessNotification from "./SuccessNotification";

type Props = {
  plugin: Plugin,
  refresh: () => void,
  onClose: () => void,

  // context props
  classes: any,
  t: (key: string, params?: Object) => string
};

type State = {
  success: boolean,
  restart: boolean,
  loading: boolean,
  error?: Error
};

const styles = {
  userLabelAlignment: {
    textAlign: "left",
    marginRight: 0,
    minWidth: "9em"
  },
  userFieldFlex: {
    flexGrow: 4
  }
};

class UpdatePluginModal extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: false,
      restart: false,
      success: false
    };
  }

  onUpdateSuccess = () => {
    const { restart } = this.state;
    const { refresh, onClose } = this.props;

    const newState = {
      loading: false,
      error: undefined
    };

    if (restart) {
      waitForRestart()
        .then(() => {
          this.setState({
            ...newState,
            success: true
          });
        })
        .catch(error => {
          this.setState({
            loading: false,
            success: false,
            error
          });
        });
    } else {
      this.setState(newState, () => {
        refresh();
        onClose();
      });
    }
  };

  update = (e: Event) => {
    const { restart } = this.state;
    const { plugin } = this.props;
    this.setState({
      loading: true
    });
    e.preventDefault();
    apiClient
      .post(plugin._links.update.href + "?restart=" + restart.toString())
      .then(this.onUpdateSuccess)
      .catch(error => {
        this.setState({
          loading: false,
          error: error
        });
      });
  };

  footer = () => {
    const { onClose, t } = this.props;
    const { loading, error, restart, success } = this.state;

    let color = "primary";
    let label = "plugins.modal.update";
    if (restart) {
      color = "warning";
      label = "plugins.modal.updateAndRestart";
    }
    return (
      <ButtonGroup>
        <Button
          label={t(label)}
          color={color}
          action={this.update}
          loading={loading}
          disabled={!!error || success}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  renderDependencies() {
    const { plugin, classes, t } = this.props;

    let dependencies = null;
    if (plugin.dependencies && plugin.dependencies.length > 0) {
      dependencies = (
        <div className="media">
          <Notification type="warning">
            <strong>{t("plugins.modal.dependencyNotification")}</strong>
            <ul className={classes.listSpacing}>
              {plugin.dependencies.map((dependency, index) => {
                return <li key={index}>{dependency}</li>;
              })}
            </ul>
          </Notification>
        </div>
      );
    }
    return dependencies;
  }

  renderNotifications = () => {
    const { t } = this.props;
    const { restart, error, success } = this.state;
    if (error) {
      return (
        <div className="media">
          <ErrorNotification error={error} />
        </div>
      );
    } else if (success) {
      return (
        <div className="media">
          <SuccessNotification />
        </div>
      );
    } else if (restart) {
      return (
        <div className="media">
          <Notification type="warning">
            {t("plugins.modal.restartNotification")}
          </Notification>
        </div>
      );
    }
    return null;
  };

  handleRestartChange = (value: boolean) => {
    this.setState({
      restart: value
    });
  };

  render() {
    const { restart } = this.state;
    const { plugin, onClose, classes, t } = this.props;

    const body = (
      <>
        <div className="media">
          <div className="media-content">
            <p>{plugin.description}</p>
          </div>
        </div>
        <div className="media">
          <div className="media-content">
            <div className="field is-horizontal">
              <div
                className={classNames(
                  classes.userLabelAlignment,
                  "field-label is-inline-flex"
                )}
              >
                {t("plugins.modal.author")}:
              </div>
              <div
                className={classNames(
                  classes.userFieldFlex,
                  "field-body is-inline-flex"
                )}
              >
                {plugin.author}
              </div>
            </div>
            <div className="field is-horizontal">
              <div
                className={classNames(
                  classes.userLabelAlignment,
                  "field-label is-inline-flex"
                )}
              >
                {t("plugins.modal.currentVersion")}:
              </div>
              <div
                className={classNames(
                  classes.userFieldFlex,
                  "field-body is-inline-flex"
                )}
              >
                {plugin.version}
              </div>
            </div>
            <div className="field is-horizontal">
              <div
                className={classNames(
                  classes.userLabelAlignment,
                  "field-label is-inline-flex"
                )}
              >
                {t("plugins.modal.newVersion")}:
              </div>
              <div
                className={classNames(
                  classes.userFieldFlex,
                  "field-body is-inline-flex"
                )}
              >
                {plugin.newVersion}
              </div>
            </div>

            {this.renderDependencies()}
          </div>
        </div>
        <div className="media">
          <div className="media-content">
            <Checkbox
              checked={restart}
              label={t("plugins.modal.restart")}
              onChange={this.handleRestartChange}
              disabled={false}
            />
          </div>
        </div>
        {this.renderNotifications()}
      </>
    );

    return (
      <Modal
        title={t("plugins.modal.title.update", {
          name: plugin.displayName ? plugin.displayName : plugin.name
        })}
        closeFunction={() => onClose()}
        body={body}
        footer={this.footer()}
        active={true}
      />
    );
  }
}

export default compose(
  injectSheet(styles),
  translate("admin")
)(UpdatePluginModal);
