# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# 设置构建本机库所需的CMake的最低版本。

cmake_minimum_required(VERSION 3.4.1)

# 创建并命名一个库，将其设置为STATIC，或SHARED，并为其源代码提供相对路径。你可以定义多个库，CMake为你构建它们，Gradle会自动将您的APK与共享库打包。
# 添加自己写的c/c++源文件
add_library( # 设置库的名称
             native-lib

             # 将库设置为共享库
             SHARED

             # 提供您的源文件的相对路径
             src/main/cpp/native-lib.cpp )

# 搜索指定的预建库，并将路径存储为变量。由于CMake通过default在搜索路径中包含系统库，因此您只需指定要添加的公共NDK库的名称即可。 CMake在完成构建之前验证该库是否存在
# 依赖NDK中的库
find_library( # 设置路径变量的名称。
              log-lib

              # 指定您希望CMake定位的NDK库的名称
              log )

# 指定库CMake应链接到目标库。您可以链接多个库，例如您在构建脚本中定义的库，预构建的第三方库或系统库
# 将目标库与NDK中的库进行连接
target_link_libraries( # 指定目标库
                       native-lib

                       # 将目标库链接到NDK中包含的日志库
                       ${log-lib} )