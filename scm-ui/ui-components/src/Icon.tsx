import React from "react";
import classNames from "classnames";

type Props = {
  title?: string;
  name: string;
  color: string;
  className?: string;
};

export default class Icon extends React.Component<Props> {
  static defaultProps = {
    color: "grey-light"
  };

  render() {
    const { title, name, color, className } = this.props;
    if (title) {
      return <i title={title} className={classNames("fas", "fa-fw", "fa-" + name, `has-text-${color}`, className)} />;
    }
    return <i className={classNames("fas", "fa-" + name, `has-text-${color}`, className)} />;
  }
}
