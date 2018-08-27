//@flow
import React from 'react';

type Props = {
};

class GitAvatar extends React.Component<Props> {

  render() {
    // TODO we have to use Image from ui-components
    return <img src="/scm/images/git-logo.png" alt="Git Logo" />;
  }

}

export default GitAvatar;
