echo off

attrib -H c:\WINDOWS\SYSTEM\fmsys.sys
del c:\WINDOWS\SYSTEM\fmsys.sys

attrib -H register.lck
del register.lck

attrib -H Debug\register.lck
del Debug\register.lck

attrib -H Release\register.lck
del Release\register.lck

cls
delReg.exe

cls
