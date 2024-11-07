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
import { Changeset } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { CommaSeparatedList, Contributor, ContributorRow } from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset;
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
    return getUnique(changeset.contributors.map((contributor) => contributor.type));
  };

  const getPersonsByContributorType = (type: string) => {
    return changeset.contributors?.filter((contributor) => contributor.type === type).map((t) => t.person);
  };

  const getContributorsByType = () => {
    const availableContributorTypes: string[] = collectAvailableContributorTypes();

    const personsByContributorType = [];
    for (const type of availableContributorTypes) {
      personsByContributorType.push({ type, contributors: getPersonsByContributorType(type) });
    }
    return personsByContributorType;
  };

  return (
    <table>
      <ContributorRow label={t("changeset.contributor.type.author")}>
        <Contributor person={changeset.author} />
      </ContributorRow>

      {getContributorsByType().map((contribution) => (
        <ContributorRow key={contribution.type} label={t("changeset.contributor.type." + contribution.type)}>
          <CommaSeparatedList>
            {contribution.contributors?.map((contributor) => (
              <Contributor key={contributor.name} person={contributor} />
            ))}
          </CommaSeparatedList>
        </ContributorRow>
      ))}
      <ExtensionPoint name="changesets.contributor.table.row" props={{ changeset }} renderAll={true} />
    </table>
  );
};

export default ContributorTable;
