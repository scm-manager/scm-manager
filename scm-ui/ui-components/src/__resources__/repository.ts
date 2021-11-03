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
