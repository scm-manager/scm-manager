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
import { useTranslation } from "react-i18next";
import { extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import { EXTENSION_POINT } from "../../avatar/Avatar";
import styled from "styled-components";
import CommaSeparatedList from "../../CommaSeparatedList";
import ContributorAvatar from "./ContributorAvatar";

type Props = {
  changeset: Changeset;
};

type PersonProps = {
  person: Person;
  className?: string;
  displayTextOnly?: boolean;
};

const useAvatar = (person: Person): string | undefined => {
  const binder = useBinder();
  const factory = binder.getExtension<extensionPoints.AvatarFactory>(EXTENSION_POINT);
  if (factory) {
    return factory(person);
  }
};

const AvatarList = styled.span`
  & > :not(:last-child) {
    margin-right: 0.25em;
  }
`;

type PersonAvatarProps = {
  person: Person;
  avatar: string;
};

const ContributorWithAvatar: FC<PersonAvatarProps> = ({ person, avatar }) => {
  const [t] = useTranslation("repos");
  if (person.mail) {
    return (
      <a href={"mailto:" + person.mail} title={t("changeset.contributors.mailto") + " " + person.mail}>
        <ContributorAvatar src={avatar} alt={person.name} />
      </a>
    );
  }
  return <ContributorAvatar src={avatar} alt={person.name} title={person.name} />;
};

export const SingleContributor: FC<PersonProps> = ({ person, className, displayTextOnly }) => {
  const [t] = useTranslation("repos");
  const avatar = useAvatar(person);
  if (!displayTextOnly && avatar) {
    return <ContributorWithAvatar person={person} avatar={avatar} />;
  }
  if (person.mail) {
    return (
      <a
        className={className}
        href={"mailto:" + person.mail}
        title={t("changeset.contributors.mailto") + " " + person.mail}
      >
        {person.name}
      </a>
    );
  }
  return <span className={className}>{person.name}</span>;
};

type PersonsProps = {
  persons: ReadonlyArray<Person>;
  label: string;
  displayTextOnly?: boolean;
};

const Contributors: FC<PersonsProps> = ({ persons, label, displayTextOnly }) => {
  const binder = useBinder();

  const [t] = useTranslation("repos");
  if (persons.length === 1) {
    return (
      <>
        {t(label)} <SingleContributor person={persons[0]} displayTextOnly={displayTextOnly} />
      </>
    );
  }

  const avatarFactory = binder.getExtension(EXTENSION_POINT);
  if (avatarFactory) {
    return (
      <>
        {t(label)}{" "}
        <AvatarList>
          {persons.map((p) => (
            <ContributorWithAvatar key={p.name} person={p} avatar={avatarFactory(p)} />
          ))}
        </AvatarList>
      </>
    );
  } else {
    return (
      <>
        {t(label)}{" "}
        <span title={persons.map((person) => "- " + person.name).join("\n")}>
          {t("changeset.contributors.more", { count: persons.length })}
        </span>
      </>
    );
  }
};

const emptyListOfContributors: ReadonlyArray<Person> = [];

const ChangesetAuthor: FC<Props> = ({ changeset }) => {
  const binder = useBinder();

  const getCoAuthors = () => {
    return filterContributorsByType("Co-authored-by");
  };

  const getCommitters = () => {
    return filterContributorsByType("Committed-by");
  };

  const filterContributorsByType = (type: string) => {
    if (changeset.contributors) {
      return changeset.contributors.filter((p) => p.type === type).map((contributor) => contributor.person);
    }
    return emptyListOfContributors;
  };

  const authorLine = [];
  if (changeset.author) {
    authorLine.push(
      <Contributors persons={[changeset.author]} label={"changeset.contributors.authoredBy"} displayTextOnly={true} />
    );
  }

  const commiters = getCommitters();
  if (commiters.length > 0) {
    authorLine.push(<Contributors persons={commiters} label={"changeset.contributors.committedBy"} />);
  }

  const coAuthors = getCoAuthors();
  if (coAuthors.length > 0) {
    authorLine.push(<Contributors persons={coAuthors} label={"changeset.contributors.coAuthoredBy"} />);
  }

  // extensions
  const extensions = binder.getExtensions<extensionPoints.ChangesetsAuthorSuffix>("changesets.author.suffix", {
    changeset
  });
  if (extensions) {
    authorLine.push(...extensions);
  }

  return (
    <CommaSeparatedList>
      {authorLine.map((line, index) => (
        <React.Fragment key={index}>{line}</React.Fragment>
      ))}
    </CommaSeparatedList>
  );
};

export default ChangesetAuthor;
