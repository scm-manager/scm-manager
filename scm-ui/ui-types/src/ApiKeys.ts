import { HalRepresentationWithEmbedded, Links } from "./hal";

export type ApiKeysCollection = HalRepresentationWithEmbedded<{ keys: ApiKey[] }>;

export type ApiKeyBase = {
  displayName: string;
  permissionRole: string;
};

export type ApiKey = ApiKeyBase & {
  id: string;
  created: string;
  _links: Links;
};

export type ApiKeyWithToken = ApiKey & {
  token: string;
}

export type ApiKeyCreation = ApiKeyBase;
