import React from "react";
import { Image } from "@scm-manager/ui-components";

type Props = {};

class GitAvatar extends React.Component<Props> {
  render() {
    return <Image src="/images/git-logo.png" alt="Git Logo" />;
  }
}

export default GitAvatar;
