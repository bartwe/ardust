TEMP:=old.release.todelete.${shell date +%s}

default:
        rm -Rf release
        mkdir -p release
        rm -Rf jarprepare
        mkdir -p jarprepare
        javac -g -Xlint:unchecked @jars -sourcepath src -d jarprepare `find src -iname "*.java"`
        jar cfm ardun.jar src/org/ardun/manifest.mf -C jarprepare/ .
        rm -Rf classes
        cp lwjgl_applet/applet/basic/* release/
        rm release/basicapplet.html
        cp src/index.html release/
        rm -f release/lwjgl-debug.jar.*



