// @flow
import groupByNamespace from "./groupByNamespace";

const base = {
  type: "git",
  _links: {}
};

const slartiBlueprintsFjords = {
  ...base,
  namespace: "slarti",
  name: "fjords-blueprints"
};

const slartiFjords = {
  ...base,
  namespace: "slarti",
  name: "fjords"
};

const hitchhikerRestand = {
  ...base,
  namespace: "hitchhiker",
  name: "restand"
};
const hitchhikerPuzzle42 = {
  ...base,
  namespace: "hitchhiker",
  name: "puzzle42"
};

const hitchhikerHeartOfGold = {
  ...base,
  namespace: "hitchhiker",
  name: "heartOfGold"
};

const zaphodMarvinFirmware = {
  ...base,
  namespace: "zaphod",
  name: "marvin-firmware"
};

it("should group the repositories by their namespace", () => {
  const repositories = [
    zaphodMarvinFirmware,
    slartiBlueprintsFjords,
    hitchhikerRestand,
    slartiFjords,
    hitchhikerHeartOfGold,
    hitchhikerPuzzle42
  ];

  const expected = [
    {
      name: "hitchhiker",
      repositories: [
        hitchhikerHeartOfGold,
        hitchhikerPuzzle42,
        hitchhikerRestand
      ]
    },
    {
      name: "slarti",
      repositories: [slartiFjords, slartiBlueprintsFjords]
    },
    {
      name: "zaphod",
      repositories: [zaphodMarvinFirmware]
    }
  ];

  expect(groupByNamespace(repositories)).toEqual(expected);
});
