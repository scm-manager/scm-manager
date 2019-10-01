//@flow
import React from "react";
import injectSheet from "react-jss";
import { translate } from "react-i18next";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn, Icon } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";
import classNames from "classnames";
import PluginModal from "./PluginModal";

export const PluginAction = {
  INSTALL: "install",
  UPDATE: "update",
  UNINSTALL: "uninstall"
};

type Props = {
  plugin: Plugin,
  refresh: () => void,

  // context props
  classes: any,
  t: string => string
};

type State = {
  showInstallModal: boolean,
  showUpdateModal: boolean,
  showUninstallModal: boolean
};

const styles = {
  link: {
    cursor: "pointer",
    pointerEvents: "all",
    marginBottom: "0 !important",
    padding: "0.5rem",
    border: "solid 1px #cdcdcd", // $dark-25
    borderRadius: "4px",
    "&:hover": {
      borderColor: "#9a9a9a" // $dark-50
    }
  },
  actionbar: {
    display: "flex",
    "& span + span": {
      marginLeft: "0.5rem"
    }
  }
};

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
    this.setState({ [showModal]: !oldValue });
  };

  createFooterRight = (plugin: Plugin) => {
    return <small className="level-item">{plugin.author}</small>;
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
    return (
      plugin._links && plugin._links.uninstall && plugin._links.uninstall.href
    );
  };

  createActionbar = () => {
    const { classes, t } = this.props;
    return (
      <div className={classes.actionbar}>
        {this.isInstallable() && (
          <span
            className={classNames(classes.link, "level-item")}
            onClick={() => this.toggleModal("showInstallModal")}
          >
            <Icon title={t("plugins.modal.install")} name="download" color="info" />
          </span>
        )}
        {this.isUninstallable() && (
          <span
            className={classNames(classes.link, "level-item")}
            onClick={() => this.toggleModal("showUninstallModal")}
          >
            <Icon title={t("plugins.modal.uninstall")} name="trash" color="info" />
          </span>
        )}
        {this.isUpdatable() && (
          <span
            className={classNames(classes.link, "level-item")}
            onClick={() => this.toggleModal("showUpdateModal")}
          >
            <Icon title={t("plugins.modal.update")} name="sync-alt" color="info" />
          </span>
        )}
      </div>
    );
  };

  renderModal = () => {
    const { plugin, refresh } = this.props;
    if (this.state.showInstallModal && this.isInstallable()) {
      return (
        <PluginModal
          plugin={plugin}
          pluginAction={PluginAction.INSTALL}
          refresh={refresh}
          onClose={() => this.toggleModal("showInstallModal")}
        />
      );
    } else if (this.state.showUpdateModal && this.isUpdatable()) {
      return (
        <PluginModal
          plugin={plugin}
          pluginAction={PluginAction.UPDATE}
          refresh={refresh}
          onClose={() => this.toggleModal("showUpdateModal")}
        />
      );
    } else if (this.state.showUninstallModal && this.isUninstallable()) {
      return (
        <PluginModal
          plugin={plugin}
          pluginAction={PluginAction.UNINSTALL}
          refresh={refresh}
          onClose={() => this.toggleModal("showUninstallModal")}
        />
      );
    } else {
      return null;
    }
  };

  createPendingSpinner = () => {
    const { plugin, classes } = this.props;
    return (
      <span className={classes.topRight}>
        <i
          className={classNames(
            "fas fa-spinner fa-lg fa-spin",
            plugin.markedForUninstall ? "has-text-danger" : "has-text-info"
          )}
        />
      </span>
    );
  };

  render() {
    const { plugin } = this.props;
    const avatar = this.createAvatar(plugin);
    const actionbar = this.createActionbar();
    const footerRight = this.createFooterRight(plugin);

    const modal = this.renderModal();

    return (
      <>
        <CardColumn
          action={
            this.isInstallable()
              ? () => this.toggleModal("showInstallModal")
              : null
          }
          avatar={avatar}
          title={plugin.displayName ? plugin.displayName : plugin.name}
          description={plugin.description}
          contentRight={
            plugin.pending || plugin.markedForUninstall
              ? this.createPendingSpinner()
              : actionbar
          }
          footerRight={footerRight}
        />
        {modal}
      </>
    );
  }
}

export default injectSheet(styles)(translate("admin")(PluginEntry));
