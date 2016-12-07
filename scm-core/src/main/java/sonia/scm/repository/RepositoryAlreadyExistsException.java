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
 * This {@link Exception} is thrown when trying to create a repository with 
 * the same name and type already exists.
 * 
 * @author Sebastian Sdorra
 */
public class RepositoryAlreadyExistsException extends RepositoryException
{
  private static final long serialVersionUID = -774929917214137675L;

  /**
   * Creates a new instance.
   * 
   * @param message exception message
   */
  public RepositoryAlreadyExistsException(String message) {
    super(message);
  }
  
  /**
   * Creates a new {@link RepositoryAlreadyExistsException} with an appropriate message.
   * 
   * @param repository repository that already exists
   * 
   * @return new exception with appropriate message
   */
  public static RepositoryAlreadyExistsException create(Repository repository){
      StringBuilder buffer = new StringBuilder("repository with name ");
      buffer.append(repository.getName()).append(" of type ");
      buffer.append(repository.getType()).append("already exists");
      return new RepositoryAlreadyExistsException(buffer.toString()); 
  }
}
