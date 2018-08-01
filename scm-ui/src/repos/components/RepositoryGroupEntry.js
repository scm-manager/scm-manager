//@flow
import React from "react";
import type { RepositoryGroup } from "../types/Repositories";
import injectSheet from "react-jss";
import classNames from "classnames";
import RepositoryEntry from "./RepositoryEntry";

const styles = {
  pointer: {
    cursor: "pointer"
  },
  repoGroup: {
    marginBottom: "1em"
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

  render() {
    const { group, classes } = this.props;
    const { collapsed } = this.state;

    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    let content = null;
    if (!collapsed) {
      content = group.repositories.map((repository, index) => {
        return <RepositoryEntry repository={repository} key={index} />;
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
        {content}
      </div>
    );
  }
}

export default injectSheet(styles)(RepositoryGroupEntry);
