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
import classNames from "classnames";
import styled from "styled-components";
import { Plugin } from "@scm-manager/ui-types";
import {
  apiClient,
  Button,
  ButtonGroup,
  Checkbox,
  ErrorNotification,
  Modal,
  Notification
} from "@scm-manager/ui-components";
import waitForRestart from "./waitForRestart";
import SuccessNotification from "./SuccessNotification";
import { PluginAction } from "./PluginEntry";

type Props = WithTranslation & {
  plugin: Plugin;
  pluginAction: string;
  refresh: () => void;
  onClose: () => void;
};

type State = {
  success: boolean;
  restart: boolean;
  loading: boolean;
  error?: Error;
};

const ListParent = styled.div`
  margin-right: 0;
  min-width: ${props => (props.pluginAction === PluginAction.INSTALL ? "5.5em" : "10em")};
  text-align: left;
`;

const ListChild = styled.div`
  flex-grow: 4;
`;

class PluginModal extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: false,
      restart: false,
      success: false
    };
  }

  onSuccess = () => {
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

  createPluginActionLink = () => {
    const { plugin, pluginAction } = this.props;
    const { restart } = this.state;

    let pluginActionLink = "";

    if (pluginAction === PluginAction.INSTALL) {
      pluginActionLink = plugin._links.install.href;
    } else if (pluginAction === PluginAction.UPDATE) {
      pluginActionLink = plugin._links.update.href;
    } else if (pluginAction === PluginAction.UNINSTALL) {
      pluginActionLink = plugin._links.uninstall.href;
    }
    return pluginActionLink + "?restart=" + restart.toString();
  };

  handlePluginAction = (e: Event) => {
    this.setState({
      loading: true
    });
    e.preventDefault();
    apiClient
      .post(this.createPluginActionLink())
      .then(this.onSuccess)
      .catch(error => {
        this.setState({
          loading: false,
          success: false,
          error: error
        });
      });
  };

  footer = () => {
    const { pluginAction, onClose, t } = this.props;
    const { loading, error, restart, success } = this.state;

    let color = pluginAction === PluginAction.UNINSTALL ? "warning" : "primary";
    let label = `plugins.modal.${pluginAction}`;
    if (restart) {
      color = "warning";
      label = `plugins.modal.${pluginAction}AndRestart`;
    }
    return (
      <ButtonGroup>
        <Button
          label={t(label)}
          color={color}
          action={this.handlePluginAction}
          loading={loading}
          disabled={!!error || success}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  renderDependencies() {
    const { plugin, t } = this.props;

    let dependencies = null;
    if (plugin.dependencies && plugin.dependencies.length > 0) {
      dependencies = (
        <div className="media">
          <Notification type="warning">
            <strong>{t("plugins.modal.dependencyNotification")}</strong>
            <ul>
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

  renderOptionalDependencies() {
    const { plugin, t } = this.props;

    let optionalDependencies = null;
    if (plugin.optionalDependencies && plugin.optionalDependencies.length > 0) {
      optionalDependencies = (
        <div className="media">
          <Notification type="warning">
            <strong>{t("plugins.modal.optionalDependencyNotification")}</strong>
            <ul>
              {plugin.optionalDependencies.map((optionalDependency, index) => {
                return <li key={index}>{optionalDependency}</li>;
              })}
            </ul>
          </Notification>
        </div>
      );
    }
    return optionalDependencies;
  }

  renderNotifications = () => {
    const { t, pluginAction } = this.props;
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
          <SuccessNotification pluginAction={pluginAction} />
        </div>
      );
    } else if (restart) {
      return (
        <div className="media">
          <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
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

  createRestartSectionContent = () => {
    const { restart } = this.state;
    const { plugin, pluginAction, t } = this.props;

    if (plugin._links[pluginAction + "WithRestart"]) {
      return (
        <Checkbox
          checked={restart}
          label={t("plugins.modal.restart")}
          onChange={this.handleRestartChange}
          disabled={false}
        />
      );
    } else {
      return <Notification type="warning">{t("plugins.modal.manualRestartRequired")}</Notification>;
    }
  };

  render() {
    const { plugin, pluginAction, onClose, t } = this.props;

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
              <ListParent className={classNames("field-label", "is-inline-flex")} pluginAction={pluginAction}>
                {t("plugins.modal.author")}:
              </ListParent>
              <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.author}</ListChild>
            </div>
            {pluginAction === PluginAction.INSTALL && (
              <div className="field is-horizontal">
                <ListParent className={classNames("field-label", "is-inline-flex")} pluginAction={pluginAction}>
                  {t("plugins.modal.version")}:
                </ListParent>
                <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.version}</ListChild>
              </div>
            )}
            {(pluginAction === PluginAction.UPDATE || pluginAction === PluginAction.UNINSTALL) && (
              <div className="field is-horizontal">
                <ListParent className={classNames("field-label", "is-inline-flex")}>
                  {t("plugins.modal.currentVersion")}:
                </ListParent>
                <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.version}</ListChild>
              </div>
            )}
            {pluginAction === PluginAction.UPDATE && (
              <div className="field is-horizontal">
                <ListParent className={classNames("field-label", "is-inline-flex")}>
                  {t("plugins.modal.newVersion")}:
                </ListParent>
                <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.newVersion}</ListChild>
              </div>
            )}
            {this.renderDependencies()}
            {this.renderOptionalDependencies()}
          </div>
        </div>
        <div className="media">
          <div className="media-content">{this.createRestartSectionContent()}</div>
        </div>
        {this.renderNotifications()}
      </>
    );

    return (
      <Modal
        title={t(`plugins.modal.title.${pluginAction}`, {
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

export default withTranslation("admin")(PluginModal);
