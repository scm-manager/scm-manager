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

import { Changeset, ChangesetCollection } from "@scm-manager/ui-types";

const one: Changeset = {
  id: "a88567ef1e9528a700555cad8c4576b72fc7c6dd",
  author: { mail: "scm-admin@scm-manager.org", name: "SCM Administrator" },
  date: new Date("2020-06-09T06:34:47Z"),
  description:
    "The starship Heart of Gold was the first spacecraft to make use of the Infinite Improbability Drive. The craft was stolen by then-President Zaphod Beeblebrox at the official launch of the ship, as he was supposed to be officiating the launch. Later, during the use of the Infinite Improbability Drive, the ship picked up Arthur Dent and Ford Prefect, who were floating unprotected in deep space in the same star sector, having just escaped the destruction of the same planet.\n\n",
  contributors: [
    {
      type: "Committed-by",
      person: { mail: "zaphod.beeblebrox@hitchhiker.cm", name: "Zaphod Beeblebrox" },
    },
    { type: "Co-authored-by", person: { mail: "ford.prefect@hitchhiker.com", name: "Ford Prefect" } },
  ],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/a88567ef1e9528a700555cad8c4576b72fc7c6dd",
    },
    diff: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/a88567ef1e9528a700555cad8c4576b72fc7c6dd",
    },
    sources: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/sources/a88567ef1e9528a700555cad8c4576b72fc7c6dd",
    },
    modifications: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/modifications/a88567ef1e9528a700555cad8c4576b72fc7c6dd",
    },
    diffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/a88567ef1e9528a700555cad8c4576b72fc7c6dd/parsed",
    },
  },
  _embedded: {
    tags: [],
    branches: [],
    parents: [
      {
        id: "d21cc6c359270aef2196796f4d96af65f51866dc",
        _links: {
          self: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/d21cc6c359270aef2196796f4d96af65f51866dc",
          },
          diff: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/d21cc6c359270aef2196796f4d96af65f51866dc",
          },
        },
      },
    ],
  },
};

const two: Changeset = {
  id: "d21cc6c359270aef2196796f4d96af65f51866dc",
  author: { mail: "scm-admin@scm-manager.org", name: "SCM Administrator" },
  date: new Date("2020-06-09T05:39:50Z"),
  description: 'Change heading to "Heart Of Gold"\n\n',
  contributors: [
    {
      type: "Committed-by",
      person: { mail: "zaphod.beeblebrox@hitchhiker.cm", name: "Zaphod Beeblebrox" },
    },
  ],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    diff: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    sources: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/sources/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    modifications: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/modifications/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    diffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/d21cc6c359270aef2196796f4d96af65f51866dc/parsed",
    },
  },
  _embedded: {
    tags: [],
    branches: [],
    parents: [
      {
        id: "e163c8f632db571c9aa51a8eb440e37cf550b825",
        _links: {
          self: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/e163c8f632db571c9aa51a8eb440e37cf550b825",
          },
          diff: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/e163c8f632db571c9aa51a8eb440e37cf550b825",
          },
        },
      },
    ],
  },
};

const three: Changeset = {
  id: "e163c8f632db571c9aa51a8eb440e37cf550b825",
  author: { mail: "scm-admin@scm-manager.org", name: "SCM Administrator" },
  date: new Date("2020-06-09T05:25:16Z"),
  description: "initialize repository",
  contributors: [],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/e163c8f632db571c9aa51a8eb440e37cf550b825",
    },
    diff: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/e163c8f632db571c9aa51a8eb440e37cf550b825",
    },
    sources: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/sources/e163c8f632db571c9aa51a8eb440e37cf550b825",
    },
    modifications: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/modifications/e163c8f632db571c9aa51a8eb440e37cf550b825",
    },
    diffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/e163c8f632db571c9aa51a8eb440e37cf550b825/parsed",
    },
  },
  _embedded: { tags: [], branches: [], parents: [] },
};

const four: Changeset = {
  id: "b6c6f8fbd0d490936fae7d26ffdd4695cc2a0930",
  author: { mail: "scm-admin@scm-manager.org", name: "SCM Administrator" },
  date: new Date("2020-06-09T09:23:49Z"),
  description: "Added design docs\n\n",
  contributors: [
    { type: "Co-authored-by", person: { mail: "ford.prefect@hitchhiker.com", name: "Ford Prefect" } },
    { type: "Co-authored-by", person: { mail: "zaphod.beeblebrox@hitchhiker.cm", name: "Zaphod Beeblebrox" } },
    { type: "Co-authored-by", person: { mail: "trillian@hitchhiker.cm", name: "Tricia Marie McMillan" } },
  ],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/b6c6f8fbd0d490936fae7d26ffdd4695cc2a0930",
    },
    diff: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/b6c6f8fbd0d490936fae7d26ffdd4695cc2a0930",
    },
    sources: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/sources/b6c6f8fbd0d490936fae7d26ffdd4695cc2a0930",
    },
    modifications: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/modifications/b6c6f8fbd0d490936fae7d26ffdd4695cc2a0930",
    },
    diffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/b6c6f8fbd0d490936fae7d26ffdd4695cc2a0930/parsed",
    },
  },
  _embedded: {
    tags: [],
    branches: [],
    parents: [
      {
        id: "a88567ef1e9528a700555cad8c4576b72fc7c6dd",
        _links: {
          self: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/a88567ef1e9528a700555cad8c4576b72fc7c6dd",
          },
          diff: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/a88567ef1e9528a700555cad8c4576b72fc7c6dd",
          },
        },
      },
    ],
  },
};

const five: Changeset = {
  id: "d21cc6c359270aef2196796f4d96af65f51866dc",
  author: { mail: "scm-admin@scm-manager.org", name: "SCM Administrator" },
  date: new Date("2020-06-09T05:39:50Z"),
  description: "HOG-42 Change mail to arthur@guide.galaxy\n\n",
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    diff: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    sources: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/sources/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    modifications: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/modifications/d21cc6c359270aef2196796f4d96af65f51866dc",
    },
    diffParsed: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/d21cc6c359270aef2196796f4d96af65f51866dc/parsed",
    },
  },
  _embedded: {
    tags: [],
    branches: [],
    parents: [
      {
        id: "e163c8f632db571c9aa51a8eb440e37cf550b825",
        _links: {
          self: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/changesets/e163c8f632db571c9aa51a8eb440e37cf550b825",
          },
          diff: {
            href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/diff/e163c8f632db571c9aa51a8eb440e37cf550b825",
          },
        },
      },
    ],
  },
};

const changesets: ChangesetCollection = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/branches/master/changesets/?page=0&pageSize=10",
    },
    first: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/branches/master/changesets/?page=0&pageSize=10",
    },
    last: {
      href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/branches/master/changesets/?page=0&pageSize=10",
    },
  },
  _embedded: {
    changesets: [one, two, three, four],
    branch: {
      name: "master",
      _links: {
        self: { href: "http://localhost:8081/scm/api/v2/repositories/hitchhiker/heart-of-gold/branches/master" },
      },
    },
  },
};

export { one, two, three, four, five };
export default changesets;
