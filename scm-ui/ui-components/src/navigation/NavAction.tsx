import React from 'react';

type Props = {
  icon?: string;
  label: string;
  action: () => void;
};

class NavAction extends React.Component<Props> {
  render() {
    const { label, icon, action } = this.props;

    let showIcon = null;
    if (icon) {
      showIcon = (
        <>
          <i className={icon}></i>{' '}
        </>
      );
    }

    return (
      <li>
        <a onClick={action} href="javascript:void(0);">
          {showIcon}
          {label}
        </a>
      </li>
    );
  }
}

export default NavAction;
