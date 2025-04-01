/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.store.sqlite;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Slf4j
class LoggingReadWriteLock implements ReadWriteLock {

  private static int rwLockCounter = 0;
  private static int lockCounter = 0;

  private final ReadWriteLock delegate;
  private final int nr;

  LoggingReadWriteLock(ReadWriteLock delegate) {
    this.delegate = delegate;
    synchronized (LoggingReadWriteLock.class) {
      nr = ++rwLockCounter;
    }
  }

  @Override
  public Lock readLock() {
    return new LoggingLock(nr, delegate.readLock(), "read");
  }

  @Override
  public Lock writeLock() {
    return new LoggingLock(nr, delegate.writeLock(), "write");
  }

  private static class LoggingLock implements Lock {

    private final int nr;
    private final int subNr;
    private final Lock delegate;
    private final String purpose;
    private long lockStart;

    private LoggingLock(int nr, Lock delegate, String purpose) {
      this.nr = nr;
      this.delegate = delegate;
      this.purpose = purpose;
      synchronized (LoggingReadWriteLock.class) {
        subNr = ++lockCounter;
      }
    }

    @Override
    public void lock() {
      log.trace("request {} lock for lock nr {}-{}", purpose, nr, subNr);
      delegate.lock();
      lockStart = System.nanoTime();
      log.trace("got {} lock for lock nr {}-{}", purpose, nr, subNr);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
      log.trace("try interruptibly {} lock for lock nr {}-{}", purpose, nr, subNr);
      delegate.lockInterruptibly();
      lockStart = System.nanoTime();
      log.trace("got {} lock for lock nr {}-{}", purpose, nr, subNr);
    }

    @Override
    public boolean tryLock() {
      log.trace("try {} lock for lock nr {}-{}", purpose, nr, subNr);
      boolean result = delegate.tryLock();
      if (result) {
        lockStart = System.nanoTime();
      }
      log.trace("result for {} lock for lock nr {}-{}: {}", purpose, nr, subNr, result);
      return result;
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
      log.trace("try {} lock for lock nr {}-{}", purpose, nr, subNr);
      boolean result = delegate.tryLock(l, timeUnit);
      if (result) {
        lockStart = System.nanoTime();
      }
      log.trace("result for {} lock for lock nr {}-{}: {}", purpose, nr, subNr, result);
      return result;
    }

    @Override
    public void unlock() {
      log.trace("release {} lock for lock nr {}-{}", purpose, nr, subNr);
      delegate.unlock();
      long duration = System.nanoTime() - lockStart;
      log.trace("{} lock released after {}ns for lock nr {}-{}", purpose, duration, nr, subNr);
      lockStart = 0;
    }

    @Override
    public Condition newCondition() {
      return delegate.newCondition();
    }
  }
}

