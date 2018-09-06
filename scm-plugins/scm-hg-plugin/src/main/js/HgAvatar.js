//@flow
import React from 'react';

type Props = {
};

class HgAvatar extends React.Component<Props> {

  render() {
    // TODO we have to use Image from ui-components
    return <img src="/scm/images/hg-logo.png" alt="Mercurial Logo" />;
  }

}

export default HgAvatar;
