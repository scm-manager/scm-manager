import React from 'react';

type Props = {
  title?: string;
};

class Title extends React.Component<Props> {
  render() {
    const { title } = this.props;
    if (title) {
      return <h1 className="title">{title}</h1>;
    }
    return null;
  }
}

export default Title;
