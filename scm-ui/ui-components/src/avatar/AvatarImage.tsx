import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { Image } from "..";
import { Person } from "./Avatar";
import { EXTENSION_POINT } from "./Avatar";

type Props = {
  person: Person;
};

class AvatarImage extends React.Component<Props> {
  render() {
    const { person } = this.props;

    const avatarFactory = binder.getExtension(EXTENSION_POINT);
    if (avatarFactory) {
      const avatar = avatarFactory(person);

      return <Image className="has-rounded-border" src={avatar} alt={person.name} />;
    }

    return null;
  }
}

export default AvatarImage;
