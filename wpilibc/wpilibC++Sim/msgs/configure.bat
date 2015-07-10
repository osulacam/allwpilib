:: This file is a helper for frc_gazebo_plugin configuration (cmake) on Windows
:: 
:: Usage: cd /build && ../configure
@set WS=C:\Users\peter\gz-ws

@set PROTOBUF_PATH=%WS%\protobuf-2.6.0-win64-vc12

cmake -G "NMake Makefiles"^
    -DCMAKE_INSTALL_PREFIX=build
    -DPROTOBUF_SRC_ROOT_FOLDER="%PROTOBUF_PATH%"^
    ..
