import * as React from 'react';
import { binder } from '@scm-manager/ui-extensions';
import { EXTENSION_POINT } from './Avatar';

type Props = {
  children: React.Node;
};

class AvatarWrapper extends React.Component<Props> {
  render() {
    if (binder.hasExtension(EXTENSION_POINT)) {
      return <>{this.props.children}</>;
    }
    return null;
  }
}

export default AvatarWrapper;
