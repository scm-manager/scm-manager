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

import { Changeset, Link, NamespaceAndName, Repository, Tag, TagCollection } from "@scm-manager/ui-types";
import { objectLink, requiredLink } from "./links";
import { QueryClient, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiResult } from "./base";
import { repoQueryKey } from "./keys";
import { apiClient } from "./apiclient";
import { concat } from "./urls";
import { useEffect } from "react";
import { queryClient } from "./ApiProvider";

const tagQueryKey = (repository: NamespaceAndName, tag: string) => {
  return repoQueryKey(repository, "tag", tag);
};

export const useTags = (repository: Repository): ApiResult<TagCollection> => {
  const link = requiredLink(repository, "tags");
  return useQuery<TagCollection, Error>({
    queryKey: repoQueryKey(repository, "tags"),
    queryFn: () => apiClient.get(link).then((response) => response.json()),
    // we do not populate the cache for a single tag,
    // because we have no pagination for tags and if we have a lot of them
    // the population slows us down
  });
};

export const useTagsIfAvailable = (repository: Repository): ApiResult<TagCollection> => {
  const link = objectLink(repository, "tags");
  const result = useQuery<TagCollection, Error>(
    {
      queryKey: repoQueryKey(repository, "tags"),
      queryFn: () => {
        if (!link) {
          return Promise.resolve({ _embedded: { tags: [] }, _links: {} } as TagCollection);
        }
        return apiClient.get(link).then((response) => response.json());
      },
    },
    // we do not populate the cache for a single branch,
    // because we have no pagination for branches and if we have a lot of them
    // the population slows us down
  );
  useEffect(() => {
    if (result.data) {
      queryClient.invalidateQueries({ queryKey: repoQueryKey(repository, "tags") });
    }
  }, [repository, result.data]);
  return result;
};

export const useContainedInTags = (changeset: Changeset, repository: Repository): ApiResult<TagCollection> => {
  const link = objectLink(changeset, "containedInTags");

  return useQuery<TagCollection, Error>({
    queryKey: repoQueryKey(repository, "tags", changeset.id),
    queryFn: () => {
      if (link === null) {
        return {
          _embedded: {
            tags: [],
          },
          _links: {},
        };
      }

      return apiClient.get(link).then((response) => response.json());
    },
  });
};

export const useTag = (repository: Repository, name: string): ApiResult<Tag> => {
  const link = requiredLink(repository, "tags");
  return useQuery<Tag, Error>({
    queryKey: tagQueryKey(repository, name),
    queryFn: () => apiClient.get(concat(link, encodeURIComponent(name))).then((response) => response.json()),
  });
};

const invalidateCacheForTag = (queryClient: QueryClient, repository: NamespaceAndName, tag: Tag) => {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: repoQueryKey(repository, "tags") }),
    queryClient.invalidateQueries({ queryKey: repoQueryKey(repository, "changesets") }),
    queryClient.invalidateQueries({ queryKey: repoQueryKey(repository, "changeset", tag.revision) }),
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
        "application/vnd.scmm-tagRequest+json;v=2",
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
  const { isPending, error, mutate, data } = useMutation<Tag, Error, string>({
    mutationFn: createTag(changeset, link),
    onSuccess: async (tag) => {
      queryClient.setQueryData(tagQueryKey(repository, tag.name), tag);
      await queryClient.invalidateQueries({ queryKey: tagQueryKey(repository, tag.name) });
      await invalidateCacheForTag(queryClient, repository, tag);
    },
  });
  return {
    isPending,
    error,
    create: (name: string) => mutate(name),
    tag: data,
  };
};

export const useDeleteTag = (repository: Repository) => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, Tag>({
    mutationFn: (tag) => {
      const deleteUrl = (tag._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    onSuccess: async (_, tag) => {
      queryClient.removeQueries({ queryKey: tagQueryKey(repository, tag.name) });
      await invalidateCacheForTag(queryClient, repository, tag);
    },
  });
  return {
    remove: (tag: Tag) => mutate(tag),
    isPending,
    error,
    isDeleted: !!data,
  };
};
