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
import styled from "styled-components";
import { Plugin } from "@scm-manager/ui-types";
import { CardColumn, Icon } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";
import PluginModal from "./PluginModal";

export const PluginAction = {
  INSTALL: "install",
  UPDATE: "update",
  UNINSTALL: "uninstall"
};

type Props = WithTranslation & {
  plugin: Plugin;
};

type State = {
  showInstallModal: boolean;
  showUpdateModal: boolean;
  showUninstallModal: boolean;
};

const ActionbarWrapper = styled.div`
  & span + span {
    margin-left: 0.5rem;
  }
`;

const IconWrapper = styled.span`
  margin-bottom: 0 !important;
  padding: 0.5rem;
  border: 1px solid #cdcdcd; // $dark-25
  border-radius: 4px;
  cursor: pointer;
  pointer-events: all;

  &:hover {
    border-color: #9a9a9a; // $dark-50
  }
`;

class PluginEntry extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      showInstallModal: false,
      showUpdateModal: false,
      showUninstallModal: false
    };
  }

  createAvatar = (plugin: Plugin) => {
    return <PluginAvatar plugin={plugin} />;
  };

  toggleModal = (showModal: string) => {
    const oldValue = this.state[showModal];
    this.setState({
      [showModal]: !oldValue
    });
  };

  createFooterLeft = (plugin: Plugin) => {
      return <small>{plugin.version}</small>;
  };

  createFooterRight = (plugin: Plugin) => {
    return <small className="level-item is-block shorten-text">{plugin.author}</small>;
  };

  isInstallable = () => {
    const { plugin } = this.props;
    return plugin._links && plugin._links.install && plugin._links.install.href;
  };

  isUpdatable = () => {
    const { plugin } = this.props;
    return plugin._links && plugin._links.update && plugin._links.update.href;
  };

  isUninstallable = () => {
    const { plugin } = this.props;
    return plugin._links && plugin._links.uninstall && plugin._links.uninstall.href;
  };

  createActionbar = () => {
    const { t } = this.props;
    return (
      <ActionbarWrapper className="is-flex">
        {this.isInstallable() && (
          <IconWrapper className="level-item" onClick={() => this.toggleModal("showInstallModal")}>
            <Icon title={t("plugins.modal.install")} name="download" color="info" />
          </IconWrapper>
        )}
        {this.isUninstallable() && (
          <IconWrapper className="level-item" onClick={() => this.toggleModal("showUninstallModal")}>
            <Icon title={t("plugins.modal.uninstall")} name="trash" color="info" />
          </IconWrapper>
        )}
        {this.isUpdatable() && (
          <IconWrapper className="level-item" onClick={() => this.toggleModal("showUpdateModal")}>
            <Icon title={t("plugins.modal.update")} name="sync-alt" color="info" />
          </IconWrapper>
        )}
      </ActionbarWrapper>
    );
  };

  renderModal = () => {
    const { plugin } = this.props;
    if (this.state.showInstallModal && this.isInstallable()) {
      return (
        <PluginModal
          plugin={plugin}
          pluginAction={PluginAction.INSTALL}
          onClose={() => this.toggleModal("showInstallModal")}
        />
      );
    } else if (this.state.showUpdateModal && this.isUpdatable()) {
      return (
        <PluginModal
          plugin={plugin}
          pluginAction={PluginAction.UPDATE}
          onClose={() => this.toggleModal("showUpdateModal")}
        />
      );
    } else if (this.state.showUninstallModal && this.isUninstallable()) {
      return (
        <PluginModal
          plugin={plugin}
          pluginAction={PluginAction.UNINSTALL}
          onClose={() => this.toggleModal("showUninstallModal")}
        />
      );
    } else {
      return null;
    }
  };

  createPendingSpinner = () => {
    const { plugin } = this.props;
    return <Icon className="fa-spin fa-lg" name="spinner" color={plugin.markedForUninstall ? "danger" : "info"} />;
  };

  render() {
    const { plugin } = this.props;
    const avatar = this.createAvatar(plugin);
    const actionbar = this.createActionbar();
    const footerLeft = this.createFooterLeft(plugin);
    const footerRight = this.createFooterRight(plugin);
    const modal = this.renderModal();

    return (
      <>
        <CardColumn
          action={this.isInstallable() ? () => this.toggleModal("showInstallModal") : null}
          avatar={avatar}
          title={plugin.displayName ? <strong>{plugin.displayName}</strong> : <strong>{plugin.name}</strong>}
          description={plugin.description}
          contentRight={plugin.pending || plugin.markedForUninstall ? this.createPendingSpinner() : actionbar}
          footerLeft={footerLeft}
          footerRight={footerRight}
        />
        {modal}
      </>
    );
  }
}

export default withTranslation("admin")(PluginEntry);
