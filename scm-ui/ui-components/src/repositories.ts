import { Repository } from '@scm-manager/ui-types';

// util methods for repositories

export function getProtocolLinkByType(repository: Repository, type: string) {
  let protocols = repository._links.protocol;
  if (protocols) {
    if (!Array.isArray(protocols)) {
      protocols = [protocols];
    }
    for (let proto of protocols) {
      if (proto.name === type) {
        return proto.href;
      }
    }
  }
  return null;
}
