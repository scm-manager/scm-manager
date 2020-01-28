import registerRequireContextHook from "babel-plugin-require-context-hook/register";
import Worker from "./__mocks__/workerMock";
registerRequireContextHook();
window.Worker = Worker;
