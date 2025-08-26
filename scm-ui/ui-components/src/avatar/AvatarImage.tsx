/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC } from "react";
import { Image } from "@scm-manager/ui-core";
import { EXTENSION_POINT, Person } from "./Avatar";
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

    return <Image className={classes} src={avatar} alt={person.name} crossOrigin="anonymous" />;
  }

  return null;
};

export default AvatarImage;
