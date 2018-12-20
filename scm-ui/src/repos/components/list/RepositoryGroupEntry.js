//@flow
import React from "react";
import type { RepositoryGroup, Repository } from "@scm-manager/ui-types";
import injectSheet from "react-jss";
import classNames from "classnames";
import RepositoryEntry from "./RepositoryEntry";

const styles = {
  pointer: {
    cursor: "pointer",
    fontSize: "1.5rem"
  },
  repoGroup: {
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
  group: RepositoryGroup,

  // context props
  classes: any
};

type State = {
  collapsed: boolean
};

class RepositoryGroupEntry extends React.Component<Props, State> {
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

  isLastEntry = (array: Repository[], index: number) => {
    return index === array.length - 1;
  };

  isLengthOdd = (array: Repository[]) => {
    return array.length % 2 !== 0;
  };

  isFullSize = (array: Repository[], index: number) => {
    return this.isLastEntry(array, index) && this.isLengthOdd(array);
  };

  render() {
    const { group, classes } = this.props;
    const { collapsed } = this.state;

    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    let content = null;
    if (!collapsed) {
      content = group.repositories.map((repository, index) => {
        const full = this.isFullSize(group.repositories, index);
        return (
          <RepositoryEntry repository={repository} full={full} key={index} />
        );
      });
    }
    return (
      <div className={classes.repoGroup}>
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

export default injectSheet(styles)(RepositoryGroupEntry);
