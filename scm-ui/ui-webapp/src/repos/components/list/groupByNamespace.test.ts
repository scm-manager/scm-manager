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
    slartiBlueprintsFjords,
    hitchhikerRestand,
    slartiFjords,
    hitchhikerHeartOfGold,
    hitchhikerPuzzle42,
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
    },
    {
      name: "slarti",
      namespace: { namespace: "slarti" },
      repositories: [slartiFjords, slartiBlueprintsFjords],
    },
    {
      name: "zaphod",
      namespace: { namespace: "zaphod" },
      repositories: [zaphodMarvinFirmware],
    },
  ];

  expect(groupByNamespace(repositories, namespaces)).toEqual(expected);
});
