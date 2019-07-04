//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";
import type { PluginGroup, Plugin } from "@scm-manager/ui-types";
import PluginEntry from "./PluginEntry";

const styles = {
  pointer: {
    cursor: "pointer",
    fontSize: "1.5rem"
  },
  pluginGroup: {
    marginBottom: "1em"
  },
  wrapper: {
    padding: "0 0.75rem"
  },
  clearfix: {
    clear: "both"
  }
};

type Props = {
  group: PluginGroup,

  // context props
  classes: any
};

type State = {
  collapsed: boolean
};

class PluginGroupEntry extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  toggleCollapse = () => {
    this.setState(prevState => ({
      collapsed: !prevState.collapsed
    }));
  };

  isLastEntry = (array: Plugin[], index: number) => {
    return index === array.length - 1;
  };

  isLengthOdd = (array: Plugin[]) => {
    return array.length % 2 !== 0;
  };

  isFullSize = (array: Plugin[], index: number) => {
    return this.isLastEntry(array, index) && this.isLengthOdd(array);
  };

  render() {
    const { group, classes } = this.props;
    const { collapsed } = this.state;

    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    let content = null;
    if (!collapsed) {
      content = group.plugins.map((plugin, index) => {
        const fullColumnWidth = this.isFullSize(group.plugins, index);
        return (
          <PluginEntry
            plugin={plugin}
            fullColumnWidth={fullColumnWidth}
            key={index}
          />
        );
      });
    }
    return (
      <div className={classes.pluginGroup}>
        <h2>
          <span className={classes.pointer} onClick={this.toggleCollapse}>
            <i className={classNames("fa", icon)} /> {group.name}
          </span>
        </h2>
        <hr />
        <div className={classNames("columns", "is-multiline", classes.wrapper)}>
          {content}
        </div>
        <div className={classes.clearfix} />
      </div>
    );
  }
}

export default injectSheet(styles)(PluginGroupEntry);
