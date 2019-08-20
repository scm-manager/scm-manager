//@flow
import type { Links } from "./hal";

export type Config = {
  proxyPassword: string | null,
  proxyPort: number,
  proxyServer: string,
  proxyUser: string | null,
  enableProxy: boolean,
  realmDescription: string,
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
  loginInfoUrl: string,
  _links: Links
};
