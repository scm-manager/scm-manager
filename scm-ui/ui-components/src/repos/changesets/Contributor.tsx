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
import { Person } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import ContributorAvatar from "./ContributorAvatar";

const Contributor: FC<{ person: Person }> = ({ person }) => {
  const [t] = useTranslation("repos");
  const binder = useBinder();
  const avatarFactory = binder.getExtension<extensionPoints.AvatarFactory>("avatar.factory");
  let prefix = null;
  if (avatarFactory) {
    const avatar = avatarFactory(person);
    if (avatar) {
      prefix = (
        <>
          <ContributorAvatar src={avatar} alt={person.name} crossOrigin="anonymous" />{" "}
        </>
      );
    }
  }
  if (person.mail) {
    return (
      <a href={"mailto:" + person.mail} title={t("changeset.contributors.mailto") + " " + person.mail}>
        {prefix}
        {person.name}
      </a>
    );
  }
  return <>{person.name}</>;
};

export default Contributor;
