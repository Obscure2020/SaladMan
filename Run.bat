@echo off
javac *.java && java Main
del /Q /S *.class 1>nul
taskkill /f /im java*