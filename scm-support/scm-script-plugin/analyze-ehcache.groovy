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

// see https://bitbucket.org/sdorra/scm-manager/issue/345/ehcache-eating-up-all-memory   

def humanReadable(def bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp-1);
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
}

def sizeOf(Serializable object){
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(object);
    oos.close();
    return baos.size();
}

Runtime runtime = Runtime.getRuntime();
def maxMemory = runtime.maxMemory();
def allocatedMemory = runtime.totalMemory();
def freeMemory = runtime.freeMemory();

println "memory:"
println "  - max       : " + humanReadable(maxMemory);
println "  - allocated : " + humanReadable(allocatedMemory);
println "  - free      : " + humanReadable(freeMemory);

println "";
println "";

def cacheManager = injector.getInstance(sonia.scm.cache.CacheManager.class).cacheManager;

def totalCMax = 0;

def cacheNames = cacheManager.getCacheNames();
for ( def cacheName : cacheNames ){
    def cache = cacheManager.getCache(cacheName);
    def totalMemory = cache.calculateInMemorySize();
    def average = cache.getSize() > 0 ? Math.round( totalMemory / cache.getSize() ) : 0;
    def max = cache.getCacheConfiguration().getMaxEntriesLocalHeap();
    def maxSize = average * max;
    
    totalCMax += maxSize;
    
    println cache.name + ":";
    println "  - size    : " + cache.getSize();
    println "  - maxsize : " + max;
    println "  - average : " + humanReadable( average );
    println "  - current : " + humanReadable( totalMemory );
    println "  - cmax    : " + humanReadable( maxSize );
}

println "";
println "";
println "Total CMax: " + humanReadable(totalCMax);