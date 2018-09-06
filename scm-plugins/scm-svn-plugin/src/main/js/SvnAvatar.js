//@flow
import React from 'react';

type Props = {
};

class SvnAvatar extends React.Component<Props> {

  render() {
    // TODO we have to use Image from ui-components
    return <img src="/scm/images/svn-logo.gif" alt="Subversion Logo" />;
  }

}

export default SvnAvatar;
