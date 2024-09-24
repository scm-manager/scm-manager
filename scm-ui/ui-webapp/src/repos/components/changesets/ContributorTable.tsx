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
import { Changeset, Person } from "@scm-manager/ui-types";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import { CommaSeparatedList, ContributorAvatar } from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset;
};

const SizedTd = styled.td`
  width: 10rem;
`;

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
          <ContributorAvatar src={avatar} alt={person.name} />{" "}
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

const getUnique = (items: string[]) =>
  Object.keys(
    items.reduce((result, item) => {
      if (!(item in result)) {
        result[item] = true;
      }
      return result;
    }, {} as { [type: string]: boolean })
  );

const ContributorTable: FC<Props> = ({ changeset }) => {
  const [t] = useTranslation("plugins");

  const collectAvailableContributorTypes = () => {
    if (!changeset.contributors) {
      return [];
    }
    return getUnique(changeset.contributors.map(contributor => contributor.type));
  };

  const getPersonsByContributorType = (type: string) => {
    return changeset.contributors?.filter(contributor => contributor.type === type).map(t => t.person);
  };

  const getContributorsByType = () => {
    const availableContributorTypes: string[] = collectAvailableContributorTypes();

    const personsByContributorType = [];
    for (const type of availableContributorTypes) {
      personsByContributorType.push({ type, persons: getPersonsByContributorType(type) });
    }
    return personsByContributorType;
  };

  return (
    <table>
      <tr>
        <SizedTd>{t("changeset.contributor.type.author")}:</SizedTd>
        <td>
          <Contributor person={changeset.author} />
        </td>
      </tr>
      {getContributorsByType().map(contributor => (
        <tr key={contributor.type}>
          <SizedTd>{t("changeset.contributor.type." + contributor.type)}:</SizedTd>
          <td className="is-ellipsis-overflow m-0">
            <CommaSeparatedList>
              {contributor.persons?.map(person => (
                <Contributor key={person.name} person={person} />
              ))}
            </CommaSeparatedList>
          </td>
        </tr>
      ))}
    </table>
  );
};

export default ContributorTable;
