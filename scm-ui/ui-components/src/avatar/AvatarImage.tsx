/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC } from "react";
import { Image } from "..";
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

    return <Image className={classes} src={avatar} alt={person.name} />;
  }

  return null;
};

export default AvatarImage;
