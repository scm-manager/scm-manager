//@flow
import type { Links } from "./hal";

export type Config = {
  proxyPassword: string | null,
  proxyPort: number,
  proxyServer: string,
  proxyUser: string | null,
  enableProxy: boolean,
  realmDescription: string,
  enableRepositoryArchive: boolean,
  disableGroupingGrid: boolean,
  dateFormat: string,
  anonymousAccessEnabled: boolean,
  baseUrl: string,
  forceBaseUrl: boolean,
  loginAttemptLimit: number,
  proxyExcludes: string[],
  skipFailedAuthenticators: boolean,
  pluginUrl: string,
  loginAttemptLimitTimeout: number,
  enabledXsrfProtection: boolean,
  namespaceStrategy: string,
  _links: Links
};
