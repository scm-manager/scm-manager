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
import { Changeset } from "@scm-manager/ui-types/src";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

type Props = {
  changeset: Changeset;
};

const SizedTd = styled.td`
  width: 10rem;
`;

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
          <a title={changeset?.author?.mail} href={"mailto:" + changeset?.author?.mail}>
            {changeset?.author?.name}
          </a>
        </td>
      </tr>
      {getTrailersByType().map(trailer => (
        <tr>
          <SizedTd>{t("changeset.trailer.type." + trailer.type) + ":"}</SizedTd>
          <td className="shorten-text is-marginless">
            {trailer.persons
              .map(person => (
                <a title={person.mail} href={"mailto:" + person.mail}>
                  {person.name}
                </a>
              ))
              .reduce((prev, curr) => [prev, ", ", curr])}
          </td>
        </tr>
      ))}
    </table>
  );
};

export default ContributorTable;
