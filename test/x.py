class Anca:
    event died(when)
    
    def kill(self, time):
        return self.died(time)

anca = Anca()

print Anca.died
print anca.died

on anca.died(a, *b):
    print "HANDLER", a, " -> ", b
    # return True

on anca.died(*c):
    print "HANDLER2", c


if not anca.kill("now"):
    print "nothing handled killing anca :("
