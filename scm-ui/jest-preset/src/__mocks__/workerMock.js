function WorkerMock() {}

WorkerMock.prototype.addEventListener = function() {};
WorkerMock.prototype.removeEventListener = function() {};
WorkerMock.prototype.postMessage = function() {};

module.exports = WorkerMock;
