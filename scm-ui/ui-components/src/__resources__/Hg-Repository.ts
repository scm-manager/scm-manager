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
  contact: "",
  creationDate: "2020-03-23T08:26:01.164Z",
  description: "",
  healthCheckFailures: [],
  lastModified: "2020-03-23T08:26:01.876Z",
  namespace: "scmadmin",
  name: "Mercurial",
  type: "hg",
  _links: {
    self: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial" },
    delete: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial" },
    update: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial" },
    permissions: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/permissions/" },
    protocol: [
      { href: "ssh://scmadmin@localhost:4567/repo/scmadmin/mercurial", name: "ssh" },
      { href: "http://localhost:8081/scm/repo/scmadmin/mercurial", name: "http" }
    ],
    tags: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/tags/" },
    branches: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/branches/" },
    incomingChangesets: {
      href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/incoming/{source}/{target}/changesets",
      templated: true
    },
    incomingDiff: {
      href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/incoming/{source}/{target}/diff",
      templated: true
    },
    incomingDiffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/incoming/{source}/{target}/diff/parsed",
      templated: true
    },
    changesets: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/changesets/" },
    sources: { href: "http://localhost:8081/scm/api/v2/repositories/scmadmin/mercurial/sources/" },
    authorMappingConfig: {
      href: "http://localhost:8081/scm/api/v2/authormapping/configuration/scmadmin/mercurial"
    },
    unfavorize: { href: "http://localhost:8081/scm/api/v2/unfavorize/scmadmin/mercurial" },
    favorites: [
      { href: "http://localhost:8081/scm/api/v2/unfavorize/scmadmin/mercurial", name: "unfavorize" },
      { href: "http://localhost:8081/scm/api/v2/favorize/scmadmin/mercurial", name: "favorize" }
    ]
  }
};
