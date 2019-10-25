import React from "react";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import { Image } from "@scm-manager/ui-components";

type Props = {
  repository: Repository;
};

class RepositoryAvatar extends React.Component<Props> {
  render() {
    const { repository } = this.props;
    return (
      <p className="image is-64x64">
        <ExtensionPoint
          name="repos.repository-avatar"
          props={{
            repository
          }}
        >
          <Image src="/images/blib.jpg" alt="Logo" />
        </ExtensionPoint>
      </p>
    );
  }
}

export default RepositoryAvatar;
