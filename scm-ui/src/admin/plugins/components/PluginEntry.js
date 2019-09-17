//@flow
import React from "react";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";
import classNames from "classnames";
import PluginModal from "./PluginModal";

const PluginAction = {
  INSTALL: "install",
  UPDATE: "update",
  UNINSTALL: "uninstall"
};

type Props = {
  plugin: Plugin,
  refresh: () => void,

  // context props
  classes: any
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
    padding: "0.5rem",
    border: "solid 1px var(--dark-25)",
    borderRadius: "4px",
    "&:hover": {
      borderColor: "var(--dark-50)"
    }
  },
  topRight: {
    position: "absolute",
    right: 0,
    top: 0
  },
  layout: {
    "& .level": {
      paddingBottom: "0.5rem"
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
    const { classes } = this.props;
    return (
      <div className={classNames(classes.actionbar, classes.topRight)}>
        {this.isInstallable() && (
          <span
            className={classNames(classes.link, "level-item")}
            onClick={() => this.toggleModal("showInstallModal")}
          >
            <i className="fas fa-download has-text-info" />
          </span>
        )}
        {this.isUninstallable() && (
          <span
            className={classNames(classes.link, "level-item")}
            onClick={() => this.toggleModal("showUninstallModal")}
          >
            <i className="fas fa-trash has-text-info" />
          </span>
        )}
        {this.isUpdatable() && (
          <span
            className={classNames(classes.link, "level-item")}
            onClick={() => this.toggleModal("showUpdateModal")}
          >
            <i className="fas fa-sync-alt has-text-info" />
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
    const { plugin, classes } = this.props;
    const avatar = this.createAvatar(plugin);
    const actionbar = this.createActionbar();
    const footerRight = this.createFooterRight(plugin);

    const modal = this.renderModal();

    return (
      <>
        <CardColumn
          className={classes.layout}
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

export default injectSheet(styles)(PluginEntry);
