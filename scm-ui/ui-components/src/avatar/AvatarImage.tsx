import React, { FC } from "react";
import { Image } from "..";
import { Person } from "./Avatar";
import { EXTENSION_POINT } from "./Avatar";
import { useBinder } from "@scm-manager/ui-extensions";

type Props = {
  person: Person;
  representation?: "rounded" | "rounded-border";
  className?: string;
};

const AvatarImage: FC<Props> = ({ person, representation = "rounded-border", className }) => {
  const binder = useBinder();
  const avatarFactory = binder.getExtension(EXTENSION_POINT);
  if (avatarFactory) {
    const avatar = avatarFactory(person);

    let classes = representation === "rounded" ? "is-rounded" : "has-rounded-border";
    if (className) {
      classes += " " + className;
    }

    return <Image className={classes} src={avatar} alt={person.name} />;
  }

  return null;
};

export default AvatarImage;
