import React from 'react';
import { translate } from 'react-i18next';
import styled from 'styled-components';
import { Tag } from '@scm-manager/ui-components';

type Props = {
  defaultBranch?: boolean;

  // context props
  t: (p: string) => string;
};

const LeftMarginTag = styled(Tag)`
  vertical-align: inherit;
  margin-left: 0.75rem;
`;

class DefaultBranchTag extends React.Component<Props> {
  render() {
    const { defaultBranch, t } = this.props;

    if (defaultBranch) {
      return <LeftMarginTag color="dark" label={t('branch.defaultTag')} />;
    }
    return null;
  }
}

export default translate('repos')(DefaultBranchTag);
