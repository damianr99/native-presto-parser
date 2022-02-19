# GRAAL_HOME ?= /Library/Java/JavaVirtualMachines/openjdk17-graalvm/Contents/Home/bin/
GRAAL_HOME ?= ~/tmp/graalvm-ce-java17-22.0.0.2/bin
SO_EXT :=.so

OS := $(shell uname -s)
ifeq ($(OS), "Darwin")
	# On ARM Apple, need to force x86 mode, since native-image only builds x86 today.
	CFLAGS += -arch x86_64
	SO_EXT :=.dylib
endif

build:
	mvn package

c:
	$(CC) $(CFLAGS) -o a.out -Wall -Itarget/native src/main/c/prestoparser.c target/native/NativePrestoParser$(SO_EXT)

python:
	c++ -O -shared -std=gnu++11 -Itarget/native -I`python3 -c 'import pybind11 as _; print(_.__path__[0])'`/include `python3-config --cflags --ldflags --libs` src/main/python/pypresto.cpp -o pypresto.so -fPIC target/native/NativePrestoParser$(SO_EXT)

native: build
	mkdir -p target/native
	set -e ;\
	CLASSPATH=$$(mvn -q exec:exec -Dexec.classpathScope="compile" -Dexec.executable="echo" -Dexec.args="%classpath") ;\
	cd target/native ;\
	$(GRAAL_HOME)/native-image -cp $$CLASSPATH NativePrestoParser --no-fallback --shared -H:Name=NativePrestoParser
