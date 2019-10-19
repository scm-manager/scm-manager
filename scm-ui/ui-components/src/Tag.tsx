import * as React from 'react';
import classNames from 'classnames';

type Props = {
  className?: string;
  color: string;
  icon?: string;
  label: string;
  title?: string;
  onClick?: () => void;
  onRemove?: () => void;
};

class Tag extends React.Component<Props> {
  static defaultProps = {
    color: 'light',
  };

  render() {
    const {
      className,
      color,
      icon,
      label,
      title,
      onClick,
      onRemove,
    } = this.props;
    let showIcon = null;
    if (icon) {
      showIcon = (
        <>
          <i className={classNames('fas', `fa-${icon}`)} />
          &nbsp;
        </>
      );
    }
    let showDelete = null;
    if (onRemove) {
      showDelete = <a className="tag is-delete" onClick={onRemove} />;
    }

    return (
      <>
        <span
          className={classNames('tag', `is-${color}`, className)}
          title={title}
          onClick={onClick}
        >
          {showIcon}
          {label}
        </span>
        {showDelete}
      </>
    );
  }
}

export default Tag;
