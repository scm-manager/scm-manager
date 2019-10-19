import React from 'react';

type Props = {
  value: string;
};

class Label extends React.Component<Props> {
  render() {
    return <label>{this.props.value}</label>;
  }
}

export default Label;
