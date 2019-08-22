//@flow
import React from "react";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";
import PluginModal from "./PluginModal";
import classNames from "classnames";

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
    pointerEvents: "all"
  },
  spinner: {
    position: "absolute",
    right: 0,
    top: 0
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

  createFooterLeft = () => {
    const { classes } = this.props;
    if (this.isInstallable()) {
      return (
        <span
          className={classNames(classes.link, "level-item")}
          onClick={this.toggleModal}
        >
          <i className="fas fa-download has-text-info" />
        </span>
      );
    }
  };

  createPendingSpinner = () => {
    const { plugin, classes } = this.props;
    if (plugin.pending) {
      return (
        <span className={classes.spinner}>
          <i className="fas fa-spinner fa-spin has-text-info" />
        </span>
      );
    }
    return null;
  };

  render() {
    const { plugin, refresh } = this.props;
    const { showModal } = this.state;
    const avatar = this.createAvatar(plugin);
    const footerLeft = this.createFooterLeft();
    const footerRight = this.createFooterRight(plugin);

    const modal = showModal ? (
      <PluginModal
        plugin={plugin}
        refresh={refresh}
        onClose={this.toggleModal}
      />
    ) : null;

    return (
      <>
        <CardColumn
          action={this.isInstallable() ? this.toggleModal : null}
          avatar={avatar}
          title={plugin.displayName ? plugin.displayName : plugin.name}
          description={plugin.description}
          contentRight={this.createPendingSpinner()}
          footerLeft={footerLeft}
          footerRight={footerRight}
        />
        {modal}
      </>
    );
  }
}

export default injectSheet(styles)(PluginEntry);
