import React, {FC} from "react";
import { binder } from "@scm-manager/ui-extensions";
import { Image } from "..";
import { Person } from "./Avatar";
import { EXTENSION_POINT } from "./Avatar";

type Props = {
  person: Person;
  representation?: "rounded" | "rounded-border";
};

const AvatarImage:FC<Props> = ({person, representation = "rounded-border"}) => {
    const avatarFactory = binder.getExtension(EXTENSION_POINT);
    if (avatarFactory) {
      const avatar = avatarFactory(person);

      const className = representation === "rounded" ? "is-rounded" : "has-rounded-border";

      return <Image className={className} src={avatar} alt={person.name} />;
    }

    return null;
};

export default AvatarImage;
