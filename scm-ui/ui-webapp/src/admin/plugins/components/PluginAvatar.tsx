import React from 'react';
import { ExtensionPoint } from '@scm-manager/ui-extensions';
import { Plugin } from '@scm-manager/ui-types';
import { Image } from '@scm-manager/ui-components';

type Props = {
  plugin: Plugin;
};

export default class PluginAvatar extends React.Component<Props> {
  render() {
    const { plugin } = this.props;
    return (
      <p className="image is-64x64">
        <ExtensionPoint
          name="plugins.plugin-avatar"
          props={{
            plugin,
          }}
        >
          <Image
            src={plugin.avatarUrl ? plugin.avatarUrl : '/images/blib.jpg'}
            alt="Logo"
          />
        </ExtensionPoint>
      </p>
    );
  }
}
