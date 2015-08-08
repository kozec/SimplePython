class FakeDict:
    def keys(self):
        return "abce"
    def __getitem__(self, key):
        if key == 'a' : return 12
        if key == 'b' : return 19
        if key == 'c' : return 4851
        if key == 'e' : return 3
        raise IndexError

fak = FakeDict()
for x in fak: print x, fak[x]

def anca(a, b=1, c=2, e=3):
    print (a, b, c, e)

def mara(*a, **b):
    print a, b

tup = ('a', 'b', 'c')
mara(1,2,3,z=17,*tup,**FakeDict())
