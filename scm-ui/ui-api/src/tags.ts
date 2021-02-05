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
 *
 */

import { Changeset, NamespaceAndName, Repository, Tag, TagCollection } from "@scm-manager/ui-types";
import { requiredLink } from "./links";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { ApiResult } from "./base";
import { repoQueryKey } from "./keys";
import { apiClient } from "@scm-manager/ui-components";

const tagQueryKey = (repository: NamespaceAndName, tag: string) => {
  return repoQueryKey(repository, "tag", tag);
};

export const useTags = (repository: Repository): ApiResult<TagCollection> => {
  const queryClient = useQueryClient();
  const link = requiredLink(repository, "tags");
  return useQuery<TagCollection, Error>(
    repoQueryKey(repository, "tags"),
    () => apiClient.get(link).then(response => response.json()),
    {
      onSuccess: tags => {
        tags._embedded.tags.forEach(tag => {
          // TODO does this make sense?
          // do we want every tag in the cache
          // it slows down the rendering of the tag creation modal
          queryClient.setQueryData(tagQueryKey(repository, tag.name), tag);
        });
      }
    }
  );
};

const createTag = (changeset: Changeset, link: string) => {
  return (name: string) => {
    return apiClient
      .post(link, {
        name,
        revision: changeset.id
      })
      .then(response => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then(response => response.json());
  };
};

export const useCreateTag = (repository: Repository, changeset: Changeset) => {
  const queryClient = useQueryClient();
  const link = requiredLink(changeset, "tag");
  const { isLoading, error, mutate, data } = useMutation<Tag, Error, string>(createTag(changeset, link), {
    onSuccess: async tag => {
      queryClient.setQueryData(tagQueryKey(repository, tag.name), tag);
      await queryClient.invalidateQueries(repoQueryKey(repository, "tags"));
      await queryClient.invalidateQueries(repoQueryKey(repository, "changeset", tag.revision));
    }
  });
  return {
    isLoading,
    error,
    create: (name: string) => mutate(name),
    tag: data
  };
};
