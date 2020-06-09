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
import { Changeset } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { useBinder } from "@scm-manager/ui-extensions";
import { EXTENSION_POINT, Person } from "../../avatar/Avatar";
import Image from "../../Image";
import styled from "styled-components";

type Props = {
  changeset: Changeset;
};

type PersonProps = {
  person: Person;
  displayTextOnly?: boolean;
};

const useAvatar = (person: Person): string | undefined => {
  const binder = useBinder();
  const factory: (person: Person) => string | undefined = binder.getExtension(EXTENSION_POINT);
  if (factory) {
    return factory(person);
  }
};

const AvatarImage = styled(Image)`
  width: 1em;
  height: 1em;
  margin-right: 0.25em;
  vertical-align: middle !important;
  margin-bottom: 0.2em;
  border-radius: 0.25em;
`;

type PersonAvatarProps = {
  person: Person;
  avatar: string;
};

const PersonAvatar: FC<PersonAvatarProps> = ({ person, avatar }) => {
  const [t] = useTranslation("repos");
  const img = <AvatarImage src={avatar} alt={person.name} title={person.name} />;
  if (person.mail) {
    return (
      <a href={"mailto:" + person.mail} title={t("changeset.author.mailto") + " " + person.mail}>
        {img}
      </a>
    );
  }
  return img;
};

const SinglePerson: FC<PersonProps> = ({ person, displayTextOnly }) => {
  const [t] = useTranslation("repos");
  const avatar = useAvatar(person);
  if (!displayTextOnly && avatar) {
    return <PersonAvatar person={person} avatar={avatar} />;
  }
  if (person.mail) {
    return (
      <a href={"mailto:" + person.mail} title={t("changeset.author.mailto") + " " + person.mail}>
        {person.name}
      </a>
    );
  }
  return <>{person.name}</>;
};

type PersonsProps = {
  persons: Person[];
  label: string;
  displayTextOnly?: boolean;
};

const Persons: FC<PersonsProps> = ({ persons, label, displayTextOnly }) => {
  const binder = useBinder();

  const [t] = useTranslation("repos");
  if (persons.length === 1) {
    return (
      <>
        {t(label)} <SinglePerson person={persons[0]} displayTextOnly={displayTextOnly} />
      </>
    );
  }

  const avatarFactory = binder.getExtension(EXTENSION_POINT);
  if (avatarFactory) {
    return (
      <>
        {t(label)}{" "}
        {persons.map(p => (
          <PersonAvatar person={p} avatar={avatarFactory(p)} />
        ))}
      </>
    );
  } else {
    return (
      <>
        {t(label)}{" "}
        <a title={label + ":\n" + persons.map(person => "- " + person.name).join("\n")}>
          {t("changesets.authors.more", { count: persons.length })}
        </a>
      </>
    );
  }
};

const ChangesetAuthor: FC<Props> = ({ changeset }) => {
  const [t] = useTranslation("repos");
  const binder = useBinder();

  const getCoAuthors = () => {
    return filterTrailersByType("Co-authored-by");
  };

  const getCommitters = () => {
    return filterTrailersByType("Committed-by");
  };

  const filterTrailersByType = (trailerType: string) => {
    return changeset.trailers.filter(p => p.trailerType === trailerType).map(trailer => trailer.person);
  };

  const authorLine = [];
  if (changeset.author) {
    authorLine.push(
      <Persons
        persons={[changeset.author]}
        label={"changesets.authors.authoredBy"}
        displayTextOnly={true}
      />
    );
  }

  const commiters = getCommitters();
  if (commiters.length > 0) {
    authorLine.push(<Persons persons={commiters} label={"changesets.authors.committedBy"} />);
  }

  const coAuthors = getCoAuthors();
  if (coAuthors.length > 0) {
    authorLine.push(<Persons persons={coAuthors} label={"changesets.authors.coAuthoredBy"} />);
  }

  // extensions
  const extensions = binder.getExtensions("changesets.author.suffix", { changeset });
  if (extensions) {
    coAuthors.push(...extensions);
  }

  return (
    <>
      {authorLine.map((p, i) => {
        if (i === 0) {
          return <>{p}</>;
        } else if (i + 1 === authorLine.length) {
          return (
            <>
              {" "}
              {t("changesets.authors.and")} {p}{" "}
            </>
          );
        } else {
          return <>, {p}</>;
        }
      })}
    </>
  );
};

export default ChangesetAuthor;
