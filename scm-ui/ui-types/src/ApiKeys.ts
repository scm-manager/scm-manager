import { HalRepresentation, HalRepresentationWithEmbedded } from "./hal";

export type ApiKeysCollection = HalRepresentationWithEmbedded<{ keys: ApiKey[] }>;

export type ApiKeyBase = {
  displayName: string;
  permissionRole: string;
};

export type ApiKey = HalRepresentation &
  ApiKeyBase & {
    id: string;
    created: string;
  };

export type ApiKeyWithToken = ApiKey & {
  token: string;
};

export type ApiKeyCreation = ApiKeyBase;
