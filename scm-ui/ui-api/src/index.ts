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

import * as urls from "./urls";

export { urls };

export * from "./errors";
export * from "./apiclient";

export * from "./base";
export * from "./login";
export * from "./groups";
export * from "./users";
export * from "./suggestions";
export * from "./userSuggestions";
export * from "./groupSuggestions";
export * from "./namespaceSuggestions";
export * from "./repositories";
export * from "./namespaces";
export * from "./branches";
export * from "./changesets";
export * from "./tags";
export * from "./config";
export * from "./admin";
export * from "./plugins";
export * from "./repository-roles";
export * from "./permissions";
export * from "./sources";
export * from "./import";
export * from "./diff";
export * from "./notifications";
export * from "./alerts";
export * from "./configLink";
export * from "./apiKeys";
export * from "./publicKeys";
export * from "./fileContent";
export * from "./history";
export * from "./contentType";
export * from "./annotations";
export * from "./search";
export * from "./loginInfo";
export * from "./useInvalidation";
export * from "./usePluginCenterAuthInfo";
export * from "./compare";
export * from "./utils";
export * from "./links";
export * from "./localStorage";
export { useNamespaceOptions, useGroupOptions, useUserOptions } from "./useAutocompleteOptions";

export { default as ApiProvider } from "./ApiProvider";
export * from "./ApiProvider";

export * from "./LegacyContext";
export * from "./NamespaceAndNameContext";
export * from "./RepositoryContext";
export * from "./RepositoryRevisionContext";
