//@flow
import * as React from "react";

type Props = {
  left?: React.Node,
  right?: React.Node
};

export default class Level extends React.Component<Props> {
  render() {
    const { left, right } = this.props;
    return (
      <div className="level">
        <div className="level-left">{left}</div>
        <div className="level-right">{right}</div>
      </div>
    );
  }
}
