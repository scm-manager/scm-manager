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
import { Changeset, Person } from "@scm-manager/ui-types";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { useBinder } from "@scm-manager/ui-extensions";
import { ContributorAvatar, CommaSeparatedList } from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset;
};

const SizedTd = styled.td`
  width: 10rem;
`;

const Contributor: FC<{ person: Person }> = ({ person }) => {
  const [t] = useTranslation("repos");
  const binder = useBinder();
  const avatarFactory = binder.getExtension("avatar.factory");
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
      <a href={"mailto:" + person.mail} title={t("changeset.author.mailto") + " " + person.mail}>
        {prefix}
        {person.name}
      </a>
    );
  }
  return <>{person.name}</>;
};

const ContributorTable: FC<Props> = ({ changeset }) => {
  const [t] = useTranslation("plugins");

  const collectAvailableTrailerTypes = () => {
    // @ts-ignore
    return [...new Set(changeset.trailers.map(trailer => trailer.trailerType))];
  };

  const getPersonsByTrailersType = (type: string) => {
    return changeset.trailers?.filter(trailer => trailer.trailerType === type).map(t => t.person);
  };

  const getTrailersByType = () => {
    const availableTrailerTypes: string[] = collectAvailableTrailerTypes();

    const personsByTrailerType = [];
    for (const type of availableTrailerTypes) {
      personsByTrailerType.push({ type, persons: getPersonsByTrailersType(type) });
    }
    return personsByTrailerType;
  };

  return (
    <table>
      <tr>
        <SizedTd>{t("changeset.trailer.type.author") + ":"}</SizedTd>
        <td>
          <Contributor person={changeset.author} />
        </td>
      </tr>
      {getTrailersByType().map(trailer => (
        <tr key={trailer.type}>
          <SizedTd>{t("changeset.trailer.type." + trailer.type) + ":"}</SizedTd>
          <td className="is-ellipsis-overflow is-marginless">
            <CommaSeparatedList>
              {trailer.persons.map(person => (
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
