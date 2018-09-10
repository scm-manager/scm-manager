//@flow
import React from "react";
import { Image } from "@scm-manager/ui-components";

type Props = {
};

class HgAvatar extends React.Component<Props> {

  render() {
    return <Image src="/scm/images/hg-logo.png" alt="Mercurial Logo" />;
  }

}

export default HgAvatar;
