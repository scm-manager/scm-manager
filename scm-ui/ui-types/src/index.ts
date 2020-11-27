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

export { Action } from "./Action";
export { Link, Links, Collection, PagedCollection } from "./hal";

export { Me } from "./Me";
export { DisplayedUser, User } from "./User";
export { Group, Member } from "./Group";

export {
  Repository,
  RepositoryCollection,
  RepositoryGroup,
  RepositoryCreation,
  Namespace,
  NamespaceCollection
} from "./Repositories";
export { RepositoryType, RepositoryTypeCollection } from "./RepositoryTypes";

export { Branch, BranchRequest } from "./Branches";

export { Person } from "./Person";

export { Changeset, Contributor, ParentChangeset } from "./Changesets";

export { Signature } from "./Signature";

export { AnnotatedSource, AnnotatedLine } from "./Annotate";

export { Tag } from "./Tags";

export { Config, AnonymousMode } from "./Config";

export { IndexResources } from "./IndexResources";

export { Permission, PermissionCreateEntry, PermissionCollection } from "./RepositoryPermissions";

export { SubRepository, File } from "./Sources";

export { SelectValue, AutocompleteObject } from "./Autocomplete";

export { Plugin, PluginCollection, PluginGroup, PendingPlugins } from "./Plugin";

export { RepositoryRole } from "./RepositoryRole";

export { NamespaceStrategies } from "./NamespaceStrategies";
