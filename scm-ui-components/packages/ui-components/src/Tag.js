//@flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  className?: string,
  color?: string,
  icon?: string,
  label: string,
  title?: string,
  onClick?: () => void
};

class Tag extends React.Component<Props> {
  static defaultProps = {
    color: "light"
  };

  render() {
    const { className, color, icon, label, title, onClick } = this.props;
    let showIcon = null;
    if (icon) {
      showIcon = (
        <>
          <i className={classNames("fas", `fa-${icon}`)} />&nbsp;
        </>
      );
    }

    return (
      <span
        className={classNames("tag", `is-${color}`, className)}
        title={title}
        onClick={onClick}
      >
        {showIcon}
        {label}
      </span>
    );
  }
}

export default Tag;
