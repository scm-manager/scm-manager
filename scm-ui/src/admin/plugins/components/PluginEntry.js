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

  createFooterLeft = (plugin: Plugin) => {
    const { classes } = this.props;
    if (plugin.pending) {
      return (
        <span className="level-item">
          <i className="fas fa-spinner fa-spin has-text-info" />
        </span>
      );
    } else if (
      plugin._links &&
      plugin._links.install &&
      plugin._links.install.href
    ) {
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

  render() {
    const { plugin, refresh } = this.props;
    const { showModal } = this.state;
    const avatar = this.createAvatar(plugin);
    const footerLeft = this.createFooterLeft(plugin);
    const footerRight = this.createFooterRight(plugin);

    const modal = showModal ? (
      <PluginModal
        plugin={plugin}
        refresh={refresh}
        onClose={this.toggleModal}
      />
    ) : null;

    // TODO: Add link to plugin page below
    return (
      <>
        <CardColumn
          link="#"
          avatar={avatar}
          title={plugin.displayName ? plugin.displayName : plugin.name}
          description={plugin.description}
          footerLeft={footerLeft}
          footerRight={footerRight}
        />
        {modal}
      </>
    );
  }
}

export default injectSheet(styles)(PluginEntry);
