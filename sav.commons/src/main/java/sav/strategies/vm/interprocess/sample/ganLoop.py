from sys import stdin
from sys import stdout

flag = 1
while (flag):
   x = stdin.read(1)
   userinput = stdin.readline()
   betAmount = int(userinput)
   data = stdin.readline()
   print ("@@GanOutputStart@@")
   print ("x=",x)
   print ("userinput=",userinput)
   print ("betAmount=",betAmount)
   print ("data=", data)
   print ("@@GanOutputEnd@@")
   stdout.flush()
print ("finished!")

