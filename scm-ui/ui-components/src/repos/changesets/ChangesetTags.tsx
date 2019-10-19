import React from 'react';
import { Changeset } from '@scm-manager/ui-types';
import ChangesetTag from './ChangesetTag';
import ChangesetTagsCollapsed from './ChangesetTagsCollapsed';

type Props = {
  changeset: Changeset;
};

class ChangesetTags extends React.Component<Props> {
  getTags = () => {
    const { changeset } = this.props;
    return changeset._embedded.tags || [];
  };

  render() {
    const tags = this.getTags();

    if (tags.length === 1) {
      return <ChangesetTag tag={tags[0]} />;
    } else if (tags.length > 1) {
      return <ChangesetTagsCollapsed tags={tags} />;
    } else {
      return null;
    }
  }
}

export default ChangesetTags;
