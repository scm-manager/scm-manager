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

import groupByNamespace from "./groupByNamespace";

const base = {
  type: "git",
  _links: {},
};

const slartiBlueprintsFjords = {
  ...base,
  namespace: "slarti",
  name: "fjords-blueprints",
};

const slartiFjords = {
  ...base,
  namespace: "slarti",
  name: "fjords",
};

const hitchhikerRestand = {
  ...base,
  namespace: "hitchhiker",
  name: "restand",
};
const hitchhikerPuzzle42 = {
  ...base,
  namespace: "hitchhiker",
  name: "puzzle42",
};

const hitchhikerHeartOfGold = {
  ...base,
  namespace: "hitchhiker",
  name: "heartOfGold",
};

const zaphodMarvinFirmware = {
  ...base,
  namespace: "zaphod",
  name: "marvin-firmware",
};

it("should group the repositories by their namespace", () => {
  const repositories = [
    zaphodMarvinFirmware,
    slartiFjords,
    slartiBlueprintsFjords,
    hitchhikerHeartOfGold,
    hitchhikerPuzzle42,
    hitchhikerRestand,
  ];
  const namespaces = {
    _embedded: {
      namespaces: [{ namespace: "hitchhiker" }, { namespace: "slarti" }, { namespace: "zaphod" }],
    },
  };

  const expected = [
    {
      name: "hitchhiker",
      namespace: { namespace: "hitchhiker" },
      repositories: [hitchhikerHeartOfGold, hitchhikerPuzzle42, hitchhikerRestand],
      currentPageOffset: 0,
    },
    {
      name: "slarti",
      namespace: { namespace: "slarti" },
      repositories: [slartiFjords, slartiBlueprintsFjords],
      currentPageOffset: 3,
    },
    {
      name: "zaphod",
      namespace: { namespace: "zaphod" },
      repositories: [zaphodMarvinFirmware],
      currentPageOffset: 5,
    },
  ];

  expect(groupByNamespace(repositories, namespaces)).toEqual(expected);
});
