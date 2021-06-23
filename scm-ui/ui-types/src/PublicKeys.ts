import { HalRepresentation, HalRepresentationWithEmbedded } from "./hal";

export type PublicKeysCollection = HalRepresentationWithEmbedded<{
  keys: PublicKey[];
}>;

export type PublicKeyBase = {
  displayName: string;
  raw: string;
};

export type PublicKey = HalRepresentation & PublicKeyBase & {
  id: string;
  created?: string;
};

export type PublicKeyCreation = PublicKeyBase;
