import React from 'react';
import Tag from '../../Tag';

type Props = {
  icon: string;
  label: string;
};

class ChangesetTagBase extends React.Component<Props> {
  render() {
    const { icon, label } = this.props;
    return <Tag color="info" icon={icon} label={label} />;
  }
}

export default ChangesetTagBase;
