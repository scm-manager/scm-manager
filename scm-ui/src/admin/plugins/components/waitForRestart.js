// @flow
import { apiClient } from "@scm-manager/ui-components";

const waitForRestart = () => {
  const endTime = Number(new Date()) + 10000;
  let started = false;

  const executor = (resolve, reject) => {
    // we need some initial delay
    if (!started) {
      started = true;
      setTimeout(executor, 100, resolve, reject);
    } else {
      apiClient
        .get("")
        .then(resolve)
        .catch(() => {
          if (Number(new Date()) < endTime) {
            setTimeout(executor, 500, resolve, reject);
          } else {
            reject(new Error("timeout reached"));
          }
        });
    }
  };

  return new Promise<void>(executor);
};

export default waitForRestart;
