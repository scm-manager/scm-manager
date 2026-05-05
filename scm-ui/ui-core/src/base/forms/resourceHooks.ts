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

import { apiClient, requiredLink } from "@scm-manager/ui-api";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { HalRepresentation, Link } from "@scm-manager/ui-types";

type QueryKeyPair = [singular: string, plural: string];
type LinkOrHalLink = string | [entity: HalRepresentation, link: string] | HalRepresentation;
const unwrapLink = (input: LinkOrHalLink, linkName: string) => {
  if (Array.isArray(input)) {
    return requiredLink(input[0], input[1]);
  } else if (typeof input === "string") {
    return input;
  } else {
    return (input._links[linkName] as Link).href;
  }
};

type MutationResult<I, O = unknown> = {
  submit: (resource: I) => Promise<O>;
  isLoading: boolean;
  error: Error | null;
  submissionResult?: O;
};

type MutatingResourceOptions = {
  contentType?: string;
};

const createResource = <I, O = never>(link: string, contentType: string) => {
  return (payload: I): Promise<O> => {
    return apiClient
      .post(link, payload, contentType)
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

type CreateResourceOptions = MutatingResourceOptions;

/**
 * @beta
 * @since 2.41.0
 */
export const useCreateResource = <I, O>(
  link: string,
  [entityKey, collectionName]: QueryKeyPair,
  idFactory: (createdResource: O) => string,
  { contentType = "application/json" }: CreateResourceOptions = {},
): MutationResult<I, O> => {
  const queryClient = useQueryClient();
  const { mutateAsync, data, isPending, error } = useMutation<O, Error, I>({
    mutationFn: createResource<I, O>(link, contentType),
    onSuccess: (result) => {
      queryClient.setQueryData([entityKey, idFactory(result)], result);
      return queryClient.invalidateQueries({ queryKey: [collectionName] });
    },
  });
  return {
    submit: (payload: I) => mutateAsync(payload),
    isLoading: isPending,
    error,
    submissionResult: data,
  };
};

type UpdateResourceOptions = MutatingResourceOptions & {
  collectionName?: QueryKeyPair;
};

/**
 * @beta
 * @since 2.41.0
 */
export const useUpdateResource = <T>(
  link: LinkOrHalLink,
  idFactory: (createdResource: T) => string,
  {
    contentType = "application/json",
    collectionName: [entityQueryKey, collectionName] = ["", ""],
  }: UpdateResourceOptions = {},
): MutationResult<T> => {
  const queryClient = useQueryClient();
  const { mutateAsync, isPending, error, data } = useMutation<unknown, Error, T>({
    mutationFn: (resource) => apiClient.put(unwrapLink(link, "update"), resource, contentType),
    onSuccess: async (_, payload) => {
      await queryClient.invalidateQueries({
        queryKey: entityQueryKey ? [entityQueryKey, idFactory(payload)] : [idFactory(payload)],
      });
      if (collectionName) {
        await queryClient.invalidateQueries({ queryKey: [collectionName] });
      }
    },
  });
  return {
    submit: (resource: T) => mutateAsync(resource),
    isLoading: isPending,
    error,
    submissionResult: data,
  };
};

type DeleteResourceOptions = {
  collectionName?: QueryKeyPair;
};

/**
 * @beta
 * @since 2.41.0
 */
export const useDeleteResource = <T extends HalRepresentation>(
  idFactory: (createdResource: T) => string,
  { collectionName: [entityQueryKey, collectionName] = ["", ""] }: DeleteResourceOptions = {},
): MutationResult<T> => {
  const queryClient = useQueryClient();
  const { mutateAsync, isPending, error, data } = useMutation<unknown, Error, T>({
    mutationFn: (resource) => {
      const deleteUrl = (resource._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    onSuccess: async (_, resource) => {
      const id = idFactory(resource);
      await queryClient.removeQueries({ queryKey: entityQueryKey ? [entityQueryKey, id] : [id] });
      if (collectionName) {
        await queryClient.invalidateQueries({ queryKey: [collectionName] });
      }
    },
  });
  return {
    submit: (resource: T) => mutateAsync(resource),
    isLoading: isPending,
    error,
    submissionResult: data,
  };
};
