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

export default {
  contact: "heart-of-gold@hitchhiker.com",
  creationDate: "2020-03-23T08:26:01.164Z",
  description: "The starship Heart of Gold was the first spacecraft to make use of the Infinite Improbability Drive",
  healthCheckFailures: [],
  lastModified: "2020-03-23T08:26:01.876Z",
  namespace: "hitchhiker",
  name: "heartOfGold",
  type: "git",
  _links: {
    self: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git" },
    delete: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git" },
    update: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git" },
    permissions: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/permissions/" },
    protocol: [
      { href: "ssh://scmadmin@localhost:4567/repo/scmadmin/Git", name: "ssh" },
      { href: "http://localhost:8081/scm/repo/scmadmin/Git", name: "http" },
    ],
    tags: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/tags/" },
    branches: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/branches/" },
    incomingChangesets: {
      href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/incoming/{source}/{target}/changesets",
      templated: true,
    },
    incomingDiff: {
      href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/incoming/{source}/{target}/diff",
      templated: true,
    },
    incomingDiffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/incoming/{source}/{target}/diff/parsed",
      templated: true,
    },
    changesets: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/changesets/" },
    sources: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/Git/sources/" },
    authorMappingConfig: {
      href: "http://localhost:8081/scm/api/v2/authormapping/configuration/scmadmin/Git",
    },
    unfavorize: { href: "http://localhost:8081/scm/api/v2/unfavorize/scmadmin/Git" },
    favorites: [
      { href: "http://localhost:8081/scm/api/v2/unfavorize/scmadmin/Git", name: "unfavorize" },
      { href: "http://localhost:8081/scm/api/v2/favorize/scmadmin/Git", name: "favorize" },
    ],
  },
};
