//@flow
import * as React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

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
  name: string,
  elements: React.Node[],

  // context props
  classes: any
};

type State = {
  collapsed: boolean
};

class CardColumnGroup extends React.Component<Props, State> {
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

  isLastEntry = (array: React.Node[], index: number) => {
    return index === array.length - 1;
  };

  isLengthOdd = (array: React.Node[]) => {
    return array.length % 2 !== 0;
  };

  isFullSize = (array: React.Node[], index: number) => {
    return this.isLastEntry(array, index) && this.isLengthOdd(array);
  };

  render() {
    const { name, elements, classes } = this.props;
    const { collapsed } = this.state;

    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    let content = null;
    if (!collapsed) {
      content = elements.map((entry, index) => {
        const fullColumnWidth = this.isFullSize(elements, index);
        const sizeClass = fullColumnWidth ? "is-full" : "is-half";
        return (
          <div
            className={classNames(
              "box",
              "box-link-shadow",
              "column",
              "is-clipped",
              sizeClass
            )}
            key={index}
          >
            {entry}
          </div>
        );
      });
    }
    return (
      <div className={classes.repoGroup}>
        <h2>
          <span className={classes.pointer} onClick={this.toggleCollapse}>
            <i className={classNames("fa", icon)} /> {name}
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

export default injectSheet(styles)(CardColumnGroup);
