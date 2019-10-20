import React from "react";
import Icon from "./Icon";

type Props = {
  className?: string;
};

export default class HelpIcon extends React.Component<Props> {
  render() {
    const { className } = this.props;
    return (
      <Icon name="question-circle" color="blue-light" className={className} />
    );
  }
}
