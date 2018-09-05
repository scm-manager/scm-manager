//@flow
import React from "react";
import { Image } from "@scm-manager/ui-components";

type Props = {
};

class SvnAvatar extends React.Component<Props> {

  render() {
    return <Image src="/scm/images/svn-logo.gif" alt="Subversion Logo" />;
  }

}

export default SvnAvatar;
