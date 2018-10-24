//@flow
import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import type { Changeset } from "@scm-manager/ui-types";
import { Image } from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset
};

class AvatarImage extends React.Component<Props> {
  render() {
    const { changeset } = this.props;

    const avatarFactory = binder.getExtension("changeset.avatar-factory");
    if (avatarFactory) {
      const avatar = avatarFactory(changeset);

      return (
        <Image
          className="has-rounded-border"
          src={avatar}
          alt={changeset.author.name}
        />
      );
    }

    return null;
  }
}

export default AvatarImage;
