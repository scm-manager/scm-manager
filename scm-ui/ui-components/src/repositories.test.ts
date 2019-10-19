import { Repository } from '@scm-manager/ui-types';
import { getProtocolLinkByType } from './repositories';

describe('getProtocolLinkByType tests', () => {
  it('should return the http protocol link', () => {
    const repository: Repository = {
      namespace: 'scm',
      name: 'core',
      type: 'git',
      _links: {
        protocol: [
          {
            name: 'http',
            href: 'http://scm.scm-manager.org/repo/scm/core',
          },
        ],
      },
    };

    const link = getProtocolLinkByType(repository, 'http');
    expect(link).toBe('http://scm.scm-manager.org/repo/scm/core');
  });

  it('should return the http protocol link from multiple protocols', () => {
    const repository: Repository = {
      namespace: 'scm',
      name: 'core',
      type: 'git',
      _links: {
        protocol: [
          {
            name: 'http',
            href: 'http://scm.scm-manager.org/repo/scm/core',
          },
          {
            name: 'ssh',
            href: 'git@scm.scm-manager.org:scm/core',
          },
        ],
      },
    };

    const link = getProtocolLinkByType(repository, 'http');
    expect(link).toBe('http://scm.scm-manager.org/repo/scm/core');
  });

  it('should return the http protocol, even if the protocol is a single link', () => {
    const repository: Repository = {
      namespace: 'scm',
      name: 'core',
      type: 'git',
      _links: {
        protocol: {
          name: 'http',
          href: 'http://scm.scm-manager.org/repo/scm/core',
        },
      },
    };

    const link = getProtocolLinkByType(repository, 'http');
    expect(link).toBe('http://scm.scm-manager.org/repo/scm/core');
  });

  it('should return null, if such a protocol does not exists', () => {
    const repository: Repository = {
      namespace: 'scm',
      name: 'core',
      type: 'git',
      _links: {
        protocol: [
          {
            name: 'http',
            href: 'http://scm.scm-manager.org/repo/scm/core',
          },
          {
            name: 'ssh',
            href: 'git@scm.scm-manager.org:scm/core',
          },
        ],
      },
    };

    const link = getProtocolLinkByType(repository, 'awesome');
    expect(link).toBeNull();
  });

  it('should return null, if no protocols are available', () => {
    const repository: Repository = {
      namespace: 'scm',
      name: 'core',
      type: 'git',
      _links: {},
    };

    const link = getProtocolLinkByType(repository, 'http');
    expect(link).toBeNull();
  });
});
