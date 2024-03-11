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
import { Changeset, Link, NamespaceAndName, Repository, Tag, TagCollection } from "@scm-manager/ui-types";
import { objectLink, requiredLink } from "./links";
import { QueryClient, useMutation, useQuery, useQueryClient } from "react-query";
import { ApiResult } from "./base";
import { repoQueryKey } from "./keys";
import { apiClient } from "./apiclient";
import { concat } from "./urls";

const tagQueryKey = (repository: NamespaceAndName, tag: string) => {
  return repoQueryKey(repository, "tag", tag);
};

export const useTags = (repository: Repository): ApiResult<TagCollection> => {
  const link = requiredLink(repository, "tags");
  return useQuery<TagCollection, Error>(
    repoQueryKey(repository, "tags"),
    () => apiClient.get(link).then((response) => response.json())
    // we do not populate the cache for a single tag,
    // because we have no pagination for tags and if we have a lot of them
    // the population slows us down
  );
};

export const useContainedInTags = (changeset: Changeset, repository: Repository): ApiResult<TagCollection> => {
  const link = objectLink(changeset, "containedInTags");

  return useQuery<TagCollection, Error>(repoQueryKey(repository, "tags", changeset.id), () => {
    if (link === null) {
      return {
        _embedded: {
          tags: [],
        },
        _links: {},
      };
    }

    return apiClient.get(link).then((response) => response.json());
  });
};

export const useTag = (repository: Repository, name: string): ApiResult<Tag> => {
  const link = requiredLink(repository, "tags");
  return useQuery<Tag, Error>(tagQueryKey(repository, name), () =>
    apiClient.get(concat(link, name)).then((response) => response.json())
  );
};

const invalidateCacheForTag = (queryClient: QueryClient, repository: NamespaceAndName, tag: Tag) => {
  return Promise.all([
    queryClient.invalidateQueries(repoQueryKey(repository, "tags")),
    queryClient.invalidateQueries(repoQueryKey(repository, "changesets")),
    queryClient.invalidateQueries(repoQueryKey(repository, "changeset", tag.revision)),
  ]);
};

const createTag = (changeset: Changeset, link: string) => {
  return (name: string) => {
    return apiClient
      .post(
        link,
        {
          name,
          revision: changeset.id,
        },
        "application/vnd.scmm-tagRequest+json;v=2"
      )
      .then((response) => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then((response) => response.json());
  };
};

export const useCreateTag = (repository: Repository, changeset: Changeset) => {
  const queryClient = useQueryClient();
  const link = requiredLink(changeset, "tag");
  const { isLoading, error, mutate, data } = useMutation<Tag, Error, string>(createTag(changeset, link), {
    onSuccess: async (tag) => {
      queryClient.setQueryData(tagQueryKey(repository, tag.name), tag);
      await queryClient.invalidateQueries(tagQueryKey(repository, tag.name));
      await invalidateCacheForTag(queryClient, repository, tag);
    },
  });
  return {
    isLoading,
    error,
    create: (name: string) => mutate(name),
    tag: data,
  };
};

export const useDeleteTag = (repository: Repository) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Tag>(
    (tag) => {
      const deleteUrl = (tag._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, tag) => {
        queryClient.removeQueries(tagQueryKey(repository, tag.name));
        await invalidateCacheForTag(queryClient, repository, tag);
      },
    }
  );
  return {
    remove: (tag: Tag) => mutate(tag),
    isLoading,
    error,
    isDeleted: !!data,
  };
};
