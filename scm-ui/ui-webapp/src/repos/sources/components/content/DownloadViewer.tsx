import React from 'react';
import { translate } from 'react-i18next';
import { File } from '@scm-manager/ui-types';
import { DownloadButton } from '@scm-manager/ui-components';

type Props = {
  t: (p: string) => string;
  file: File;
};

class DownloadViewer extends React.Component<Props> {
  render() {
    const { t, file } = this.props;
    return (
      <div className="has-text-centered">
        <DownloadButton
          url={file._links.self.href}
          displayName={t('sources.content.downloadButton')}
        />
      </div>
    );
  }
}

export default translate('repos')(DownloadViewer);
