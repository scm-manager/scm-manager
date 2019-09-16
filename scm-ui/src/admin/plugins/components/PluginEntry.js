//@flow
import React from "react";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";
import classNames from "classnames";
import InstallPluginModal from "./InstallPluginModal";
import UpdatePluginModal from "./UpdatePluginModal";

type Props = {
  plugin: Plugin,
  refresh: () => void,

  // context props
  classes: any
};

type State = {
  showModal: boolean
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
  }
};

class PluginEntry extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      showModal: false
    };
  }

  createAvatar = (plugin: Plugin) => {
    return <PluginAvatar plugin={plugin} />;
  };

  toggleModal = () => {
    this.setState(prevState => ({
      showModal: !prevState.showModal
    }));
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

  createActionbar = () => {
    const { classes } = this.props;
    if (this.isInstallable()) {
      return (
        <span
          className={classNames(classes.link, classes.topRight, "level-item")}
          onClick={this.toggleModal}
        >
          <i className="fas fa-download has-text-info" />
        </span>
      );
    } else if (this.isUpdatable()) {
      return (
        <span
          className={classNames(classes.link, classes.topRight, "level-item")}
          onClick={this.toggleModal}
        >
          <i className="fas fa-sync-alt has-text-info" />
        </span>
      );
    }
  };

  renderModal = () => {
    const { plugin, refresh } = this.props;
    if (this.isInstallable()) {
      return (
        <InstallPluginModal
          plugin={plugin}
          refresh={refresh}
          onClose={this.toggleModal}
        />
      );
    } else if (this.isUpdatable()) {
      return (
        <UpdatePluginModal
          plugin={plugin}
          refresh={refresh}
          onClose={this.toggleModal}
        />
      );
    }
  };

  createPendingSpinner = () => {
    const { plugin, classes } = this.props;
    if (plugin.pending) {
      return (
        <span className={classes.topRight}>
          <i className="fas fa-spinner fa-spin has-text-info" />
        </span>
      );
    }
    return null;
  };

  render() {
    const { plugin, classes } = this.props;
    const { showModal } = this.state;
    const avatar = this.createAvatar(plugin);
    const actionbar = this.createActionbar();
    const footerRight = this.createFooterRight(plugin);

    const modal = showModal ? this.renderModal() : null;

    return (
      <>
        <CardColumn
          className={classes.layout}
          action={this.isInstallable() ? this.toggleModal : null}
          avatar={avatar}
          title={plugin.displayName ? plugin.displayName : plugin.name}
          description={plugin.description}
          contentRight={
            plugin.pending ? this.createPendingSpinner() : actionbar
          }
          footerRight={footerRight}
        />
        {modal}
      </>
    );
  }
}

export default injectSheet(styles)(PluginEntry);
