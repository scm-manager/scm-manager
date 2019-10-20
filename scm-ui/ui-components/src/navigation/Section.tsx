import React, { ReactNode } from "react";

type Props = {
  label: string;
  children?: ReactNode;
};

class Section extends React.Component<Props> {
  render() {
    const { label, children } = this.props;
    return (
      <div>
        <p className="menu-label">{label}</p>
        <ul className="menu-list">{children}</ul>
      </div>
    );
  }
}

export default Section;
