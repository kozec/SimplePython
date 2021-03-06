"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
Builtin module
 
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

# all python doable builtin functions are here

import math

def abs(x):
    if x > 0:
        return True
    return False

def all(iterable):
    for i in iterable:
        if not i: return False
    return True

def any(iterable):
    for i in iterable:
        if i: return True 
    return False

def callable(object):
    if object is None: return False
    t = type(object)
    if t == function: return True
    if t == method: return True
    if t == type: return True
    if t == javamethod: return True
    return hasattr(object, "__call__")

def cmp(x, y):
    if x < y:
        return -1
    elif x == y:
        return 0
    else:
        return 1
    
def divmod(a, b):
    return int(math.floor(a/b)), int(a%b)

class iter(object):
    class callable_iterator(object):
        def __init__(self, fnc, __sentinel):
            self.__sentinel = __sentinel
            self.__fnc = fnc
        def __iter__(self):
            return self
        def next(self):
            value = self.__fnc()
            if (value == self.__sentinel):
                raise StopIteration()
            return value
    
    def __init__(self, itervalue, sentinel=None):
        if hasattr(itervalue, "__iter__"):
            return itervalue.__iter__()
        elif callable(itervalue):
            return sentinel_iter(itervalue, sentinel)
        else:
            self.__data_source = itervalue
            self.__index = 0
            
    def __iter__(self):
        return self
        
    def next(self):
        try:
            i = self.__index
            self.__index += 1
            return self.__data_source[i]
        except IndexError:
            raise StopIteration()

class enumerate(object):
    def __init__(self, seq, start=0):
        self.__it = iter(seq)
        self.__start = 0
        
    def __iter__(self):
        return self
    
    def next(self):
        i = self.__start
        self.__start += 1
        return i, self.__it.next()

def filter(f, it):
    l = []
    for i in it:
        if f(i):
            l.append(i)
    return l

def issubclass(cls, classinfo):
    if type(classinfo) == tuple:
        for c in classinfo:
            if issubclass(cls, c):
                return True
        return False
    return classinfo in mro(cls)

def len(s):
    if hasattr(s, "__len__"):
        return s.__len__()
    raise TypeError("len(): argument does not support len protocol")

def identity(*args):
    return args

def map(function, *iterables):
    if function is None:
        function = identity
    data = [iter(x) for x in iterables]
    result = []
    while True:
        d = []
        r = []
        for iterable in data:
            try:
                d.append(iterable.next())
                r.append(True)
            except StopIteration:
                d.append(None)
                r.append(False)
        if not any(r):
            return result
        result.append(apply(function, d))
        
def negate(x):
    if hasattr(x, "__len__"):
        return len(x) == 0
    return not_value(x)

class set(object):
    def __init__(self, iterable=None):
        if iterable is None:
        	self.__inner_map = dict()
        else:
        	self.__inner_map = { o : o for o in iterable }
    
    def __str__(self):
        return "set(" + str(self.__inner_map.keys()) + ")"
    
    def add(self, o):
        self.__inner_map[o] = o;
        
    def __contains__(self, key):
        return key in self.__inner_map
    
    def __delkey__(self, key):
        del self.__inner_map[key]
   
def range(arg1, arg2=None, arg3=1):
    if arg2 is None:
        return xrange(arg1)
    return list(xrange(arg1, arg2, arg3))

class super(object):
    def __init__(self, cls, inst=None):
        self.__cls = cls
        self.__inst = inst
        self.__mro = mro(cls)[1:]
        
    def __getattribute__(self, key):
        cls, arg = object.__getattribute__(self, "__find_applicable")(key);
        if type(arg) == boundfunction:
            if object.__getattribute__(self, "__inst") is not None:
                return method(arg.__func__, object.__getattribute__(self, "__inst"), cls)
            else:
                return arg
        else:
            return arg
        
    def __find_applicable(self, key):
        for cls in object.__getattribute__(self, "__mro"):
            if (hasattr(cls, key)):
                return cls, getattr(cls, key)
        raise AttributeError("unknown attribute " + key)
    
def close_generator(generator):
    try:
        generator.throw(GeneratorExit, None)
    except (GeneratorExit, StopIteration):
        pass
    else:
        raise RuntimeError("generator ignored GeneratorExit")
    
def typename(object):
    return type(object).__name__

def zip(*iterables):
    data = [iter(x) for x in iterables]
    result = []
    while True:
        d = []
        for iterable in data:
            try:
                d.append(iterable.next())
            except StopIteration:
                return result
        result.append(tuple(d))
        
def sum(iterable):
    it = 0
    for x in iterable:
        it += x
    return it
