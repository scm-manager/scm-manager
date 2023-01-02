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

import { apiClient, requiredLink } from "@scm-manager/ui-api";
import { useMutation, useQueryClient } from "react-query";
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

export const useCreateResource = <I, O>(
  link: string,
  [entityKey, collectionName]: QueryKeyPair,
  idFactory: (createdResource: O) => string,
  { contentType = "application/json" }: CreateResourceOptions = {}
): MutationResult<I, O> => {
  const queryClient = useQueryClient();
  const { mutateAsync, data, isLoading, error } = useMutation<O, Error, I>(createResource<I, O>(link, contentType), {
    onSuccess: (result) => {
      queryClient.setQueryData([entityKey, idFactory(result)], result);
      return queryClient.invalidateQueries(collectionName);
    },
  });
  return {
    submit: (payload: I) => mutateAsync(payload),
    isLoading,
    error,
    submissionResult: data,
  };
};

type UpdateResourceOptions = MutatingResourceOptions & {
  collectionName?: QueryKeyPair;
};

export const useUpdateResource = <T>(
  link: LinkOrHalLink,
  idFactory: (createdResource: T) => string,
  {
    contentType = "application/json",
    collectionName: [entityQueryKey, collectionName] = ["", ""],
  }: UpdateResourceOptions = {}
): MutationResult<T> => {
  const queryClient = useQueryClient();
  const { mutateAsync, isLoading, error, data } = useMutation<unknown, Error, T>(
    (resource) => apiClient.put(unwrapLink(link, "update"), resource, contentType),
    {
      onSuccess: async (_, payload) => {
        await queryClient.invalidateQueries(entityQueryKey ? [entityQueryKey, idFactory(payload)] : idFactory(payload));
        if (collectionName) {
          await queryClient.invalidateQueries(collectionName);
        }
      },
    }
  );
  return {
    submit: (resource: T) => mutateAsync(resource),
    isLoading,
    error,
    submissionResult: data,
  };
};

type DeleteResourceOptions = {
  collectionName?: QueryKeyPair;
};

export const useDeleteResource = <T extends HalRepresentation>(
  idFactory: (createdResource: T) => string,
  { collectionName: [entityQueryKey, collectionName] = ["", ""] }: DeleteResourceOptions = {}
): MutationResult<T> => {
  const queryClient = useQueryClient();
  const { mutateAsync, isLoading, error, data } = useMutation<unknown, Error, T>(
    (resource) => {
      const deleteUrl = (resource._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, resource) => {
        const id = idFactory(resource);
        await queryClient.removeQueries(entityQueryKey ? [entityQueryKey, id] : id);
        if (collectionName) {
          await queryClient.invalidateQueries(collectionName);
        }
      },
    }
  );
  return {
    submit: (resource: T) => mutateAsync(resource),
    isLoading,
    error,
    submissionResult: data,
  };
};
