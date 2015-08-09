"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
threading module
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.
 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public
License along with this library.
"""

class Thread(object):
    def __init__(self, name=None):
        self.__jthread = javainstance("jthread", self, name)
        self.executed = getattr(self.__jthread, "executed")
        
    def set_name(self, name):
        self.__jthread.setThreadName(name)
        
    def get_name(self):
        return self.__jthread.getThreadName()
        
    def start(self):
        self.__jthread.threadStart()
        
    def execute(self):
        raise NotImplementedError("thread.execute")
    
    def join(self):
        self.__jthread.waitJoin()
        
    def interrupt(self):
        self.__jthread.interruptThread()
        
    def running(self):
        return self.__jthread.threadRunning()