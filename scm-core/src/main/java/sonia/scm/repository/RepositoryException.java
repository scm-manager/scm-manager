/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

/**
 * Base class for all repository exceptions.
 *
 * @author Sebastian Sdorra
 */
public class RepositoryException extends Exception
{

  /** Field description */
  private static final long serialVersionUID = -4939196278070910058L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link RepositoryException} with null as its 
   * error detail message.
   *
   */
  public RepositoryException()
  {
    super();
  }

  /**
   * Constructs a new {@link RepositoryException} with the specified 
   * detail message.
   *
   *
   * @param message detail message
   */
  public RepositoryException(String message)
  {
    super(message);
  }

  /**
   * Constructs a new {@link RepositoryException} with the specified 
   * detail message and cause.
   *
   *
   * @param cause the cause for the exception
   */
  public RepositoryException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs a new {@link RepositoryException} with the specified 
   * detail message and cause.
   *
   *
   * @param message detail message
   * @param cause the cause for the exception
   */
  public RepositoryException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
