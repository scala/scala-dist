@echo off

rem ##########################################################################
rem # Copyright This file is copyrighted by its owner
rem #
rem # This is free software; see the distribution for copying conditions.
rem # There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
rem # PARTICULAR PURPOSE.
rem ##########################################################################

if "%OS%"=="Windows_NT" (
  @setlocal
  set SCALA_HOME=%~dp0..%
) else (
  if "%SCALA_HOME%"=="" goto error1
)

if "%1"=="" goto usage
if "%1"=="-version" goto version


call "%SCALA_HOME%\bin\sbaz" -d "%1" setup
call "%SCALA_HOME%\bin\sbaz" -d "%1" setuniverse "%SCALA_HOME%\meta\universe"
call "%SCALA_HOME%\bin\sbaz" -d "%1" install base
goto end


:version
echo sbaz-setup version 1.0
goto end

:usage
echo Usage: sbaz-setup target_directory
goto end

:end
if "%OS%"=="Windows_NT" @endlocal
